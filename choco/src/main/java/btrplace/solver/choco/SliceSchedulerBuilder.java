/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace.solver.choco;

import btrplace.solver.choco.actionModel.ActionModelUtils;
import btrplace.solver.choco.actionModel.KeepRunningVMModel;
import btrplace.solver.choco.actionModel.VMActionModel;
import btrplace.solver.choco.chocoUtil.FastImpliesEq;
import btrplace.solver.choco.chocoUtil.LocalTaskScheduler;
import btrplace.solver.choco.chocoUtil.TaskScheduler;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.*;

/**
 * Builder to create a unique slices scheduler that aggregates
 * different resources.
 *
 * @author Fabien Hermenier
 */
public class SliceSchedulerBuilder {

    private ReconfigurationProblem rp;

    private List<IntDomainVar[]> capacities;

    private List<int[]> cUsages;

    private List<IntDomainVar[]> dUsages;

    private int[] associations;

    private IntDomainVar[] cEnds;

    private IntDomainVar[] cHosters;

    private IntDomainVar[] dHosters;

    private IntDomainVar[] dStarts;

    private HashMap<UUID, int[]> non;

    /**
     * Make a new builder.
     *
     * @param rp the associated problem
     */
    public SliceSchedulerBuilder(ReconfigurationProblem rp) {
        this.rp = rp;
        capacities = new ArrayList<>();
        cUsages = new ArrayList<>();
        dUsages = new ArrayList<>();

        List<Slice> dS = new ArrayList<>();
        List<Slice> cS = new ArrayList<>();


        non = new HashMap<>();

        int dIdx = 0, cIdx = 0;

        for (VMActionModel a : rp.getVMActions()) {
            Slice c = a.getCSlice();
            Slice d = a.getDSlice();

            if (d != null && c != null) {
                non.put(a.getVM(), new int[]{dIdx, cIdx});
            }
            if (d != null) {
                dS.add(dIdx, d);
                dIdx++;
            }

            if (c != null) {
                cS.add(cIdx, c);
                cIdx++;
            }
        }


        int i = 0;
        cHosters = new IntDomainVar[cS.size()];
        cEnds = new IntDomainVar[cS.size()];
        for (Slice s : cS) {
            cHosters[i] = s.getHoster();
            cEnds[i] = s.getEnd();
            i++;

        }

        i = 0;
        dStarts = new IntDomainVar[dS.size()];
        dHosters = new IntDomainVar[dS.size()];

        for (Slice s : dS) {
            dHosters[i] = s.getHoster();
            dStarts[i] = s.getStart();
            i++;
        }


        associations = new int[dHosters.length];
        //No associations task by default, then we create the associations.
        for (i = 0; i < associations.length; i++) {
            associations[i] = LocalTaskScheduler.NO_ASSOCIATIONS;
        }
        for (Map.Entry<UUID, int[]> e : non.entrySet()) {
            int[] assoc = e.getValue();
            associations[assoc[0]] = assoc[1];
        }
    }

    /**
     * Add a dimension.
     *
     * @param capa the resource capacity of each of the nodes
     * @param cUse the resource usage of each of the cSlices
     * @param dUse the resource usage of each of the dSlices
     */
    public void add(IntDomainVar[] capa, int[] cUse, IntDomainVar[] dUse) {
        capacities.add(capa);
        cUsages.add(cUse);
        dUsages.add(dUse);
    }

    /**
     * Build the constraint.
     *
     * @return the resulting constraint
     */
    public TaskScheduler build() {
        CPSolver s = rp.getSolver();

        //We get the UB of the node capacity and the LB for the VM usage.
        int[][] capas = new int[capacities.size()][];
        int i = 0;
        for (IntDomainVar[] capaDim : capacities) {
            capas[i] = new int[capaDim.length];
            for (int j = 0; j < capaDim.length; j++) {
                capas[i][j] = capaDim[j].getSup();
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
        for (IntDomainVar[] dUseDim : dUsages) {
            dUses[i] = new int[dUseDim.length];
            for (int j = 0; j < dUseDim.length; j++) {
                dUses[i][j] = dUseDim[j].getInf();
            }
            i++;
        }
        symmetryBreakingForStayingVMs();
        IntDomainVar[] earlyStarts = ActionModelUtils.getHostingStarts(rp.getNodeActions());
        IntDomainVar[] lastEnd = ActionModelUtils.getHostingEnds(rp.getNodeActions());
        return new TaskScheduler(s.getEnvironment(),
                earlyStarts,
                lastEnd,
                capas,
                cHosters, cUses, cEnds,
                dHosters, dUses, dStarts,
                associations);
    }

    private Boolean strictlyDecreasingOrUnchanged(UUID vm) {
        //If it has non-overlapping slices
        int[] slicesIndexes = non.get(vm);
        if (slicesIndexes != null) {
            int dIdx = slicesIndexes[0];
            int cIdx = slicesIndexes[1];

            Boolean decOrStay = null;
            //Get the resources usage
            for (int d = 0; d < cUsages.size(); d++) {
                int req = dUsages.get(d)[dIdx].getInf();
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
        return false;
    }

    /**
     * Symmetry breaking for VMs that stay running, on the same node.
     *
     * @return {@code true} iff the symmetry breaking does not lead to a problem without solutions
     */
    private boolean symmetryBreakingForStayingVMs() {
        for (UUID vm : rp.getFutureRunningVMs()) {
            VMActionModel a = rp.getVMAction(vm);
            Slice dSlice = a.getDSlice();
            Slice cSlice = a.getCSlice();
            if (dSlice != null && cSlice != null) {
                IntDomainVar stay = ((KeepRunningVMModel) a).isStaying();

                Boolean ret = strictlyDecreasingOrUnchanged(vm);
                if (Boolean.TRUE.equals(ret)) {
                    //Else, the resource usage is decreasing, so
                    // we set the cSlice duration to 0 to directly reduces the resource allocation
                    if (stay.isInstantiatedTo(1)) {
                        try {
                            cSlice.getDuration().setVal(0);
                        } catch (ContradictionException ex) {
                            rp.getLogger().info("Unable to set the cSlice duration of {} to 0", cSlice.getSubject());
                            return false;
                        }
                    } else {
                        rp.getSolver().post(new FastImpliesEq(stay, cSlice.getDuration(), 0));
                    }
                } else if (Boolean.FALSE.equals(ret)) {
                    //If the resource usage will be increasing
                    //Then the duration of the dSlice can be set to 0
                    //(the allocation will be performed at the end of the reconfiguration process)
                    if (stay.isInstantiatedTo(1)) {
                        try {
                            dSlice.getDuration().setVal(0);
                        } catch (ContradictionException ex) {
                            rp.getLogger().info("Unable to set the dSlice duration of {} to 0", dSlice.getSubject());
                            return false;
                        }
                    } else {
                        rp.getSolver().post(new FastImpliesEq(stay, dSlice.getDuration(), 0));
                    }
                }
            }
        }
        return true;
    }
}
