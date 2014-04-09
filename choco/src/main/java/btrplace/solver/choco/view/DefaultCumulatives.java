/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.view;

import btrplace.model.VM;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.extensions.FastImpliesEq;
import btrplace.solver.choco.extensions.TaskScheduler;
import btrplace.solver.choco.transition.KeepRunningVM;
import btrplace.solver.choco.transition.TransitionUtils;
import btrplace.solver.choco.transition.VMTransition;
import solver.Cause;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder to create a unique slices scheduler that aggregates
 * different resources.
 *
 * @author Fabien Hermenier
 */
public class DefaultCumulatives extends AbstractCumulatives implements Cumulatives {

    private List<IntVar[]> capacities;

    /**
     * Make a new builder.
     *
     * @param p the associated problem
     */
    public DefaultCumulatives(ReconfigurationProblem p) {
        super(p);
        capacities = new ArrayList<>();
    }


    @Override
    public String getIdentifier() {
        return Cumulatives.VIEW_ID;
    }

    @Override
    public boolean insertActions(ReconfigurationProblem r, ReconfigurationPlan p) {
        return true;
    }

    @Override
    public boolean cloneVM(VM vm, VM clone) {
        return true;
    }

    /**
     * Add a dimension.
     *
     * @param c    the resource capacity of each of the nodes
     * @param cUse the resource usage of each of the cSlices
     * @param dUse the resource usage of each of the dSlices
     */
    @Override
    public void addDim(IntVar[] c, int[] cUse, IntVar[] dUse) {
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
    public boolean beforeSolve(ReconfigurationProblem p) {
        if (capacities.isEmpty()) {
            return true;
        }
        //We get the UB of the node capacity and the LB for the VM usage.
        int[][] capas = new int[capacities.size()][];
        int i = 0;
        for (IntVar[] capaDim : capacities) {
            capas[i] = new int[capaDim.length];
            for (int j = 0; j < capaDim.length; j++) {
                capas[i][j] = capaDim[j].getUB();
            }
            i++;
        }
        i = 0;
        int[][] cUses = new int[cUsages.size()][];

        for (int[] cUseDim : cUsages) {
            cUses[i++] = cUseDim;
        }

        i = 0;

        int[][] dUses = new int[dUsages.size()][];
        for (IntVar[] dUseDim : dUsages) {
            dUses[i] = new int[dUseDim.length];
            for (int j = 0; j < dUseDim.length; j++) {
                dUses[i][j] = dUseDim[j].getLB();
            }
            i++;
        }
        symmetryBreakingForStayingVMs();
        IntVar[] earlyStarts = TransitionUtils.getHostingStarts(rp.getNodeActions());
        IntVar[] lastEnd = TransitionUtils.getHostingEnds(rp.getNodeActions());
        rp.getSolver().post(
                new TaskScheduler(earlyStarts,
                        lastEnd,
                        capas,
                        cHosts, cUses, cEnds,
                        dHosts, dUses, dStarts,
                        associations,
                        rp.getSolver())
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
    private boolean symmetryBreakingForStayingVMs() {
        for (VM vm : rp.getFutureRunningVMs()) {
            VMTransition a = rp.getVMAction(vm);
            Slice dSlice = a.getDSlice();
            Slice cSlice = a.getCSlice();
            if (dSlice != null && cSlice != null) {
                BoolVar stay = ((KeepRunningVM) a).isStaying();

                Boolean ret = strictlyDecreasingOrUnchanged(vm);
                if (Boolean.TRUE.equals(ret)) {
                    //Else, the resource usage is decreasing, so
                    // we set the cSlice duration to 0 to directly reduces the resource allocation
                    if (stay.instantiatedTo(1) && !zeroDuration(cSlice)) {
                        return false;
                    } else {
                        rp.getSolver().post(new FastImpliesEq(stay, cSlice.getDuration(), 0));
                    }
                } else if (Boolean.FALSE.equals(ret)) {
                    //If the resource usage will be increasing
                    //Then the duration of the dSlice can be set to 0
                    //(the allocation will be performed at the end of the reconfiguration process)
                    if (stay.instantiatedTo(1) && !zeroDuration(dSlice)) {
                        return false;
                    } else {
                        rp.getSolver().post(new FastImpliesEq(stay, dSlice.getDuration(), 0));
                    }
                }
            }
        }
        return true;
    }

    private boolean zeroDuration(Slice s) {
        try {
            s.getDuration().instantiateTo(0, Cause.Null);
        } catch (ContradictionException ex) {
            rp.getLogger().info("Unable to set the duration of slice {} to 0", s.getSubject());
            return false;
        }
        return true;
    }

    /**
     * Builder associated to this constraint.
     */
    public static class Builder extends SolverViewBuilder {

        @Override
        public String getKey() {
            return Cumulatives.VIEW_ID;
        }

        @Override
        public Cumulatives build(ReconfigurationProblem p) {
            return new DefaultCumulatives(p);
        }

        @Override
        public List<String> getDependencies() {
            return Collections.emptyList();
        }
    }
}
