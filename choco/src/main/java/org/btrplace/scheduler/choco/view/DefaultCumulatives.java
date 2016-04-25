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

import org.btrplace.model.VM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.extensions.FastImpliesEq;
import org.btrplace.scheduler.choco.extensions.TaskScheduler;
import org.btrplace.scheduler.choco.transition.KeepRunningVM;
import org.btrplace.scheduler.choco.transition.TransitionUtils;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder to create a unique slices scheduler that aggregates
 * different resources.
 *
 * @author Fabien Hermenier
 */
public class DefaultCumulatives extends AbstractCumulatives implements Cumulatives {

    private List<List<IntVar>> capacities;

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        super.inject(ps, rp);
        capacities = new ArrayList<>();
        return true;
    }

    @Override
    public String getIdentifier() {
        return Cumulatives.VIEW_ID;
    }

    /**
     * Add a dimension.
     *  @param c    the resource capacity of each of the nodes
     * @param cUse the resource usage of each of the cSlices
     * @param dUse the resource usage of each of the dSlices
     */
    @Override
    public void addDim(List<IntVar> c, int[] cUse, IntVar[] dUse) {
        capacities.add(c);
        cUsages.add(cUse);
        dUsages.add(dUse);
    }

    /**
     * Build the constraint.
     *
     * @return the resulting constraint
     */
    @Override
    public boolean beforeSolve(ReconfigurationProblem rp) {
        super.beforeSolve(rp);
        if (rp.getSourceModel().getMapping().getNbNodes() == 0 || capacities.isEmpty()) {
            return true;
        }

        int nbDims = capacities.size();
        int nbRes = capacities.get(0).size();

        //We get the UB of the node capacity and the LB for the VM usage.
        int[][] capas = new int[nbRes][nbDims];
        int d = 0;
        for (List<IntVar> capaDim : capacities) {
            assert capaDim.size() == nbRes;
            for (int j = 0; j < capaDim.size(); j++) {
                capas[j][d] = capaDim.get(j).getUB();
            }
            d++;
        }
        assert cUsages.size() == nbDims;
        int nbCHosts = cUsages.get(0).length;
        int[][] cUses = new int[nbCHosts][nbDims];
        d = 0;
        for (int[] cUseDim : cUsages) {
            assert cUseDim.length == nbCHosts;
            for (int i = 0; i < nbCHosts; i++) {
                cUses[i][d] = cUseDim[i];
            }
            d++;
        }


        assert dUsages.size() == nbDims;
        int nbDHosts = dUsages.get(0).length;
        int[][] dUses = new int[nbDHosts][nbDims];
        d = 0;
        for (IntVar[] dUseDim : dUsages) {
            assert dUseDim.length == nbDHosts;
            for (int j = 0; j < nbDHosts; j++) {
                dUses[j][d] = dUseDim[j].getLB();
            }
            d++;
        }
        symmetryBreakingForStayingVMs(rp);
        IntVar[] earlyStarts = TransitionUtils.getHostingStarts(rp.getNodeActions());
        IntVar[] lastEnd = TransitionUtils.getHostingEnds(rp.getNodeActions());
        rp.getSolver().post(
                new TaskScheduler(earlyStarts,
                        lastEnd,
                        capas,
                        cHosts, cUses, cEnds,
                        dHosts, dUses, dStarts,
                        associations)
        );
        return true;
    }

    private Boolean strictlyDecreasingOrUnchanged(VM vm) {
        //If it has non-overlapping slices
        int[] slicesIndexes = non.get(vm);
        if (slicesIndexes == null) {
            return false;
        }
        int dIdx = slicesIndexes[0];
        int cIdx = slicesIndexes[1];

        Boolean decOrStay = null;
        //Get the resources usage
        for (int d = 0; d < cUsages.size(); d++) {
            int req = dUsages.get(d)[dIdx].getLB();
            int use = cUsages.get(d)[cIdx];
            if (decOrStay == null) {
                decOrStay = req <= use;
            } else {
                if (decOrStay && req > use) {
                    return false;
                } else if (!decOrStay && req <= use) {
                    return false;
                }
            }
        }
        return decOrStay;
    }

    /**
     * Symmetry breaking for VMs that stay running, on the same node.
     *
     * @return {@code true} iff the symmetry breaking does not lead to a problem without solutions
     */
    private boolean symmetryBreakingForStayingVMs(ReconfigurationProblem rp) {
        for (VM vm : rp.getFutureRunningVMs()) {
            VMTransition a = rp.getVMAction(vm);
            Slice dSlice = a.getDSlice();
            Slice cSlice = a.getCSlice();
            if (dSlice != null && cSlice != null) {
                BoolVar stay = ((KeepRunningVM) a).isStaying();

                Boolean ret = strictlyDecreasingOrUnchanged(vm);
                if (Boolean.TRUE.equals(ret) && !zeroDuration(rp, stay, cSlice)) {
                    return false;
                    //Else, the resource usage is decreasing, so
                    // we set the cSlice duration to 0 to directly reduces the resource allocation
                } else if (Boolean.FALSE.equals(ret) && !zeroDuration(rp, stay, dSlice)) {
                    //If the resource usage will be increasing
                    //Then the duration of the dSlice can be set to 0
                    //(the allocation will be performed at the end of the reconfiguration process)
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean zeroDuration(ReconfigurationProblem rp, BoolVar stay, Slice s) {
        if (stay.isInstantiatedTo(1)) {
            try {
                s.getDuration().instantiateTo(0, Cause.Null);
            } catch (ContradictionException ex) {
                rp.getLogger().info("Unable to set the duration of slice " + s.getSubject() + " to 0", ex);
                return false;
            }
        } else {
            rp.getSolver().post(new FastImpliesEq(stay, s.getDuration(), 0));
        }
        return true;
    }
}
