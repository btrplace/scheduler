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
    private List<Task> tasksList;
    private List<IntVar> heightsList;

    /**
     * Make a new network view.
     *
     * @param n the network view we rely on
     */
    public CNetwork(Network n) throws SchedulerException {
        net = n;
        tasksList = new ArrayList<>();
        heightsList = new ArrayList<>();
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        solver = rp.getSolver();
        source = rp.getSourceModel();

        return true;
    }

    @Override
    public String getIdentifier() { return net.getIdentifier(); }

    @Override
    public boolean beforeSolve(ReconfigurationProblem rp) throws SchedulerException {
        
        Model mo = rp.getSourceModel();
        
        // Pre-compute duration and bandwidth for each VM migration
        for (VMTransition migration : rp.getVMActions()) {

            if (migration instanceof RelocatableVM) {
                
                // Get vars from migration
                VM vm = migration.getVM();
                IntVar bandwidth = ((RelocatableVM) migration).getBandwidth();
                IntVar duration = migration.getDuration();
                Node src = rp.getSourceModel().getMapping().getVMLocation(vm);
                
                // Try to get the destination node
                Node dst;
                if (migration.getDSlice().getHoster().isInstantiated()) {
                    dst = rp.getNode(migration.getDSlice().getHoster().getValue());
                    if (src.equals(dst)) {
                        try {
                            ((RelocatableVM) migration).getBandwidth().instantiateTo(0, Cause.Null);
                            continue;
                        } catch (ContradictionException e) {
                            rp.getLogger().error("Contradiction exception when trying to instantiate bandwidth and " +
                                    " duration variables for " + vm + " migration: " + e.getMessage());
                            return false;
                        }

                    }
                }
                else {
                    // Show a warning and throw an exception
                    rp.getLogger().warn("The destination node for " + vm + " is not known, migration discarded " +
                            "from network view.");
                    throw new SchedulerException(null, "Destination node for VM '" + vm + "' should be known !");
                }

                // Check if all attributes are defined
                if (mo.getAttributes().isSet(vm, "memUsed")) {

                    // Get attribute vars
                    int memUsed = mo.getAttributes().getInteger(vm, "memUsed");
                    
                    // Get VM memory activity attributes if defined, otherwise set an idle workload on the VM
                    double hotDirtySize = 5.0;// Minimal observed value on idle VM
                    if (mo.getAttributes().isSet(vm, "hotDirtySize")) {
                        hotDirtySize = mo.getAttributes().getInteger(vm, "hotDirtySize");
                    }
                    double hotDirtyDuration = 2.0; // Minimal observed value on idle VM
                    if (mo.getAttributes().isSet(vm, "hotDirtyDuration")) {
                        hotDirtyDuration = mo.getAttributes().getInteger(vm, "hotDirtyDuration");
                    }

                    double coldDirtyRate = 0;
                    if (mo.getAttributes().isSet(vm, "coldDirtyRate")) {
                        coldDirtyRate = mo.getAttributes().getDouble(vm, "coldDirtyRate");
                    }

                    // Get the maximal bandwidth available on the migration path
                    int maxBW = net.getRouting().getMaxBW(src, dst);
                    // Enumerate different possible values for the bandwidth to allocate (< maxBW)
                    // MULTIPLE BW; eg.(step=maxBW/2) split the max BW in 2 and allow to migrate 2 migrations per link
                    // SINGLE BW: (step=maxBW) the bandwidth can not be reduced below maxBW (always migrate at max BW)
                    /*int step = maxBW;
                    List<Integer> bwEnum = new ArrayList<>();
                    for (int i = step; i <= maxBW; i += step) {
                        if (i > Math.round(hotDirtySize / hotDirtyDuration)) {
                            bwEnum.add(i);
                        }
                    }*/

                    // Compute the duration related to each enumerated bandwidth
                    double durationMin;
                    double durationColdPages;
                    double durationHotPages;
                    double durationTotal;
                    /*List<Integer> durEnum = new ArrayList<>();
                    for (Integer bw : bwEnum) {*/

                        // Cheat a bit, real is less than theoretical (8->9)
                        double bandwidth_octet = maxBW / 9.0;

                        // Estimate the duration for the current bandwidth
                        durationMin = memUsed / bandwidth_octet;
                        if (durationMin > hotDirtyDuration) {

                            durationColdPages = ((hotDirtySize + ((durationMin - hotDirtyDuration) * coldDirtyRate)) /
                                    (bandwidth_octet - coldDirtyRate));
                            durationHotPages = ((hotDirtySize / bandwidth_octet) * ((hotDirtySize / hotDirtyDuration) /
                                    (bandwidth_octet - (hotDirtySize / hotDirtyDuration))));
                            durationTotal = durationMin + durationColdPages + durationHotPages;
                        } else {
                            durationTotal = durationMin + (((hotDirtySize / hotDirtyDuration) * durationMin) /
                                    (bandwidth_octet - (hotDirtySize / hotDirtyDuration)));
                        }
                        /*durEnum.add((int) Math.round(durationTotal));
                    }*/

                    /*// USING MULTIPLE BW FOR EACH MIGRATION
                    // Create the enumerated vars
                    bandwidth = VF.enumerated("bandwidth_enum", bwEnum.stream().mapToInt(i -> i).toArray(), s);
                    duration = VF.enumerated("duration_enum", durEnum.stream().mapToInt(i -> i).toArray(), s);

                    // Associate vars using Tuples
                    Tuples tpl = new Tuples(true);
                    for (int i = 0; i < bwEnum.size(); i++) {
                        tpl.add(bwEnum.get(i), durEnum.get(i));
                    }
                    
                    // Post the table constraint
                    s.post(ICF.table(bandwidth, duration, tpl, ""));*/

                    // USING A SINGLE BW PER MIGRATION
                    try {
                        //prevent from a 0 duration when the memory usage is very low
                        int dd = (int) Math.max(1, Math.round(durationTotal));
                        duration.instantiateTo(dd, Cause.Null);
                        bandwidth.instantiateTo(maxBW, Cause.Null);
                    } catch (ContradictionException e) {
                        rp.getLogger().error("Contradiction exception when trying to instantiate bandwidth and " +
                                " duration variables for " + vm + " migration: " + e.getMessage());
                        return false;
                    }
                } else {
                    // Show a warning and throw an exception
                    rp.getLogger().warn("The 'memUsed' attribute for " + vm + " is missing, migration discarded " +
                            "from network view.");
                    throw new SchedulerException(null, "Unable to retrieve 'memUsed' attribute for the vm '" +vm+ "'");
                }
            }
        }
        
        // Links limitation
        for (Link l : net.getLinks()) {

            for (VM vm : rp.getVMs()) {
                VMTransition a = rp.getVMAction(vm);

                if (a != null && a instanceof RelocatableVM) {
                    
                    if (a.getDSlice().getHoster().isInstantiated()) {

                        if (a.getCSlice().getHoster().getValue() != a.getDSlice().getHoster().getValue()) {

                            Node src = source.getMapping().getVMLocation(vm);
                            Node dst = rp.getNode(a.getDSlice().getHoster().getValue());
                            List<Link> path = net.getRouting().getPath(src, dst);

                            // If the link is on migration path
                            if (path.contains(l)) {
                                tasksList.add(new Task(a.getStart(), a.getDuration(), a.getEnd()));
                                heightsList.add(((RelocatableVM) a).getBandwidth());
                            }
                        }
                    }
                }
            }
            if (!tasksList.isEmpty()) {
                
                // Post the cumulative constraint for the current link
                solver.post(ICF.cumulative(
                        tasksList.toArray(new Task[tasksList.size()]),
                        heightsList.toArray(new IntVar[heightsList.size()]),
                        VF.fixed(l.getCapacity(), solver),
                        true
                ));
            }
            tasksList.clear();
            heightsList.clear();
        }

        // Switches capacity limitation
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

        return true;
    }
}
