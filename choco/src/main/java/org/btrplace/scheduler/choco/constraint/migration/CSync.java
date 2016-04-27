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

package org.btrplace.scheduler.choco.constraint.migration;

import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.migration.Sync;
import org.btrplace.model.view.network.Link;
import org.btrplace.model.view.network.Network;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.constraint.ChocoConstraint;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Choco implementation of the {@link org.btrplace.model.constraint.migration.Sync} constraint.
 *
 * @author Vincent Kherbache
 */
public class CSync implements ChocoConstraint {

    private Sync sec;

    /**
     * The list of migrations to synchronize.
     */
    private List<RelocatableVM> migrationList;

    /**
     * Make a new constraint
     *
     * @param sec the Sync constraint to rely on
     */
    public CSync(Sync sec) {
        this.sec = sec;
        migrationList = new ArrayList<>();
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance model) {
        return Collections.emptySet();
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {

        Solver s = rp.getSolver();
        Model mo = rp.getSourceModel();

        // Not enough VMs
        if (sec.getInvolvedVMs().size() < 2) {
            return true;
        }

        // Get all migrations involved
        for (VM vm : sec.getInvolvedVMs()) {
            VMTransition vt = rp.getVMAction(vm);
            if (vt instanceof RelocatableVM) {
                migrationList.add((RelocatableVM) vt);
            }
        }

        // Not enough migrations
        if (migrationList.size() < 2) {
            return true;
        }

        // Get the networking view if attached
        Network net = Network.get(mo);

        for (int i = 0; i < migrationList.size(); i++) {
            for (int j = i + 1; j < migrationList.size(); j++) {
                RelocatableVM vm1 = migrationList.get(i);
                RelocatableVM vm2 = migrationList.get(j);

                // Manage network related exceptions
                if (net != null) {
                    if (!vm1.getDSlice().getHoster().isInstantiated() || !vm2.getDSlice().getHoster().isInstantiated()) {
                        RelocatableVM vm = vm2;
                        if (!vm1.getDSlice().getHoster().isInstantiated()) {
                            vm = vm1;
                        }

                        // Log an error and return false instead of throwing an exception
                        /*throw new SchedulerException(mo, "The 'Sync' constraint must know the destination " +
                                "node of the " + vm.getVM().toString() + " migration, see the 'Fence' constraint " +
                                "to manually set the destination node.");*/
                        rp.getLogger().error("The 'Sync' constraint must know the destination " +
                                "node of the " + vm.getVM().toString() + " migration, see the 'Fence' constraint " +
                                "to manually set the destination node.");
                        return false;
                    }

                    // Get src and dst nodes and compute the routes
                    Node src1 = rp.getNode(vm1.getCSlice().getHoster().getValue());
                    Node dst1 = rp.getNode(vm1.getDSlice().getHoster().getValue());
                    List<Link> route1 = net.getRouting().getPath(src1, dst1);
                    Node src2 = rp.getNode(vm2.getCSlice().getHoster().getValue());
                    Node dst2 = rp.getNode(vm2.getDSlice().getHoster().getValue());
                    List<Link> route2 = net.getRouting().getPath(src2, dst2);

                    // Check if the migrations paths share a common link
                    if (!Collections.disjoint(route1, route2)) {
                        for (Link l1 : route1) {
                            for (Link l2 : route2) {
                                if (l1.equals(l2)) {
                                    // If the common link found is already the maximal BW in one of the path => Exception
                                    if (l1.getCapacity() == net.getRouting().getMaxBW(src1, dst1) ||
                                            l1.getCapacity() == net.getRouting().getMaxBW(src2, dst2)) {
                                        rp.getLogger().error("The migrations of " + vm1.getVM().toString() +
                                                " and " + vm2.getVM().toString() + " can not be synchronized as" +
                                                " their migration paths share a common link that will slowdown" +
                                                " unnecessarily both migrations, so they must be scheduled sequentially.");
                                        return false;
                                    }
                                }
                            }
                        }
                    }

                    // Sync the start or end depending of the migration algorithm
                    IntVar firstMigSync = vm1.usesPostCopy() ? vm1.getStart() : vm1.getEnd();
                    IntVar secondMigSync = vm2.usesPostCopy() ? vm2.getStart() : vm2.getEnd();

                    //LCF.ifThen(LCF.and(moveFirst, moveSecond),
                    s.post(ICF.arithm(firstMigSync, "=", secondMigSync));
                }
            }
        }

        return true;
    }
}
