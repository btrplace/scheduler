/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.scheduler.choco.view;

import org.btrplace.model.Attributes;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.network.Link;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.Switch;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.VF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The solver part of the network view.
 * Define the maximal bandwidth allocatable for each migration according to the capacity
 * of the network elements on each migration path. Then, establish the relation between the
 * migrations duration and their allocated bandwidth based on specific VMs attributes related
 * to VM memory activity.
 *
 * @author Vincent Kherbache
 */
public class CNetwork implements ChocoView {

    /**
     * The view identifier.
     */
    public static final String VIEW_ID = "NetworkView";
    private Network net;
    private Solver solver;
    private Model source;

    /**
     * Make a new network view.
     *
     * @param n the network view we rely on
     */
    public CNetwork(Network n) throws SchedulerException {
        net = n;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        solver = rp.getSolver();
        source = rp.getSourceModel();

        return true;
    }

    @Override
    public String getIdentifier() {
        return net.getIdentifier();
    }

    @Override
    public boolean beforeSolve(ReconfigurationProblem rp) throws SchedulerException {

        Model mo = rp.getSourceModel();
        Attributes attrs = mo.getAttributes();

        // Pre-compute duration and bandwidth for each VM migration
        for (VMTransition migration : rp.getVMActions()) {

            if (!(migration instanceof RelocatableVM)) {
                continue;
            }

            // Get vars from migration
            VM vm = migration.getVM();
            IntVar bandwidth = ((RelocatableVM) migration).getBandwidth();
            IntVar duration = migration.getDuration();
            Node src = rp.getSourceModel().getMapping().getVMLocation(vm);

            // Try to get the destination node
            Node dst;

            if (!migration.getDSlice().getHoster().isInstantiated()) {
                throw new SchedulerException(null, "Destination node for VM '" + vm + "' should be known !");
            }

            if (!mo.getAttributes().isSet(vm, "memUsed")) {
                throw new SchedulerException(null, "Unable to retrieve 'memUsed' attribute for the vm '" + vm + "'");
            }

            dst = rp.getNode(migration.getDSlice().getHoster().getValue());
            if (src.equals(dst)) {
                try {
                    ((RelocatableVM) migration).getBandwidth().instantiateTo(0, Cause.Null);
                    continue;
                } catch (ContradictionException e) {
                    rp.getLogger().error("Contradiction exception when trying to instantiate bandwidth and " +
                            " duration variables for " + vm + " migration", e);
                    return false;
                }
            }

            // Get attribute vars
            int memUsed = attrs.get(vm, "memUsed", -1);

            // Get VM memory activity attributes if defined, otherwise set an idle workload on the VM
            double hotDirtySize = attrs.get(vm, "hotDirtySize", 5.0);// Minimal observed value on idle VM
            double hotDirtyDuration = attrs.get(vm, "hotDirtyDuration", 2.0); // Minimal observed value on idle VM
            double coldDirtyRate = attrs.get(vm, "coldDirtyRate", 0.0);

            // Get the maximal bandwidth available on the migration path
            int maxBW = net.getRouting().getMaxBW(src, dst);

            // Compute the duration related to each enumerated bandwidth
            double durationMin;
            double durationColdPages;
            double durationHotPages;
            double durationTotal;

            // Cheat a bit, real is less than theoretical (8->9)
            double bandwidthOctet = maxBW / 9.0;

            // Estimate the duration for the current bandwidth
            durationMin = memUsed / bandwidthOctet;
            if (durationMin > hotDirtyDuration) {

                durationColdPages = (hotDirtySize + (durationMin - hotDirtyDuration) * coldDirtyRate) /
                        (bandwidthOctet - coldDirtyRate);
                durationHotPages = (hotDirtySize / bandwidthOctet * ((hotDirtySize / hotDirtyDuration) /
                        (bandwidthOctet - (hotDirtySize / hotDirtyDuration))));
                durationTotal = durationMin + durationColdPages + durationHotPages;
            } else {
                durationTotal = durationMin + (((hotDirtySize / hotDirtyDuration) * durationMin) /
                        (bandwidthOctet - (hotDirtySize / hotDirtyDuration)));
            }

            // Instantiate the computed bandwidth and duration
            try {
                //prevent from a 0 duration when the memory usage is very low
                int dd = (int) Math.max(1, Math.round(durationTotal));
                duration.instantiateTo(dd, Cause.Null);
                bandwidth.instantiateTo(maxBW, Cause.Null);
            } catch (ContradictionException e) {
                rp.getLogger().error("Contradiction exception when trying to instantiate bandwidth and " +
                        " duration variables for " + vm + " migration: ", e);
                return false;
            }
        }

        // Add links and switches constraints
        addLinkConstraints(rp);        
        addSwitchConstraints(rp);

        return true;
    }

    /**
     * Add the cumulative constraints for each link.
     *
     * Full-duplex links are considered, two cumulative constraints are defined per link by looking at
     * the migration direction for each link on the migration path.
     *
     * @param rp the reconfiguration problem
     */
    private void addLinkConstraints(ReconfigurationProblem rp) {

        // Links limitation
        List<Task> tasksListUp = new ArrayList<>();
        List<Task> tasksListDown = new ArrayList<>();
        List<IntVar> heightsListUp = new ArrayList<>();
        List<IntVar> heightsListDown = new ArrayList<>();
        
        for (Link l : net.getLinks()) {

            for (VM vm : rp.getVMs()) {
                VMTransition a = rp.getVMAction(vm);

                if (a instanceof RelocatableVM
                        && !a.getDSlice().getHoster().isInstantiatedTo(a.getCSlice().getHoster().getValue())) {

                    Node src = source.getMapping().getVMLocation(vm);
                    Node dst = rp.getNode(a.getDSlice().getHoster().getValue());
                    List<Link> path = net.getRouting().getPath(src, dst);

                    // Check first if the link is on migration path
                    if (path.contains(l)) {
                        // Get link direction
                        Boolean upDown = net.getRouting().getLinkDirection(src, dst, l);
                        // UpLink
                        if (upDown) {
                            //tasksListUp.add(new Task(a.getStart(), a.getDuration(), a.getEnd()));
                            tasksListUp.add(((RelocatableVM) a).getMigrationTask());
                            heightsListUp.add(((RelocatableVM) a).getBandwidth());
                        }
                        // DownLink
                        else {
                            //tasksListDown.add(new Task(a.getStart(), a.getDuration(), a.getEnd()));
                            tasksListDown.add(((RelocatableVM) a).getMigrationTask());
                            heightsListDown.add(((RelocatableVM) a).getBandwidth());
                        }
                    }
                }
            }
            if (!tasksListUp.isEmpty()) {

                // Post the cumulative constraint for the current UpLink
                solver.post(ICF.cumulative(
                        tasksListUp.toArray(new Task[tasksListUp.size()]),
                        heightsListUp.toArray(new IntVar[heightsListUp.size()]),
                        VF.fixed(l.getCapacity(), solver),
                        true
                ));

                tasksListUp.clear();
                heightsListUp.clear();
            }
            if (!tasksListDown.isEmpty()) {

                // Post the cumulative constraint for the current DownLink
                solver.post(ICF.cumulative(
                        tasksListDown.toArray(new Task[tasksListDown.size()]),
                        heightsListDown.toArray(new IntVar[heightsListDown.size()]),
                        VF.fixed(l.getCapacity(), solver),
                        true
                ));

                tasksListDown.clear();
                heightsListDown.clear();
            }
        }
    }
    /**
     * Add the cumulative constraints for each blocking switch (having limited capacity)
     *
     * @param rp the reconfiguration problem
     */
    private void addSwitchConstraints(ReconfigurationProblem rp) {
        
        // Switches capacity limitation
        List<Task> tasksList = new ArrayList<>();
        List<IntVar> heightsList = new ArrayList<>();
        
        for(Switch sw : net.getSwitches()) {


            // Only if the capacity is limited
            if (sw.getCapacity() > 0) {

                for (VM vm : rp.getVMs()) {
                    VMTransition a = rp.getVMAction(vm);

                    if (a != null && a instanceof RelocatableVM) {

                        if (a.getDSlice().getHoster().isInstantiated()) {

                            if (a.getCSlice().getHoster().getValue() != a.getDSlice().getHoster().getValue()) {

                                Node src = source.getMapping().getVMLocation(vm);
                                Node dst = rp.getNode(a.getDSlice().getHoster().getValue());

                                if (!Collections.disjoint(net.getConnectedLinks(sw), net.getRouting().getPath(src, dst))) {
                                    tasksList.add(new Task(a.getStart(), a.getDuration(), a.getEnd()));
                                    heightsList.add(((RelocatableVM) a).getBandwidth());
                                }
                            }
                        }
                    }
                }

                if (!tasksList.isEmpty()) {
                    // Post the cumulative constraint for the current switch
                    solver.post(ICF.cumulative(
                            tasksList.toArray(new Task[tasksList.size()]),
                            heightsList.toArray(new IntVar[heightsList.size()]),
                            VF.fixed(sw.getCapacity(), solver),
                            true
                    ));

                    tasksList.clear();
                    heightsList.clear();
                }
            }
        }
    }
}
