/*
 * Copyright  2022 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.view;

import org.btrplace.model.VM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.extensions.StayingVMsScheduling;
import org.btrplace.scheduler.choco.extensions.TaskScheduler;
import org.btrplace.scheduler.choco.transition.KeepRunningVM;
import org.btrplace.scheduler.choco.transition.NodeTransition;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.BitSet;
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

    /**
     * Add a dimension.
     *  @param c    the resource capacity of each of the nodes
     * @param cUse the resource usage of each of the cSlices
     * @param dUse the resource usage of each of the dSlices
     */
    @Override
    public void addDim(List<IntVar> c, int[] cUse, int[] dUse) {
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
    @SuppressWarnings("squid:S3346")
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
        for (int[] dUseDim : dUsages) {
            assert dUseDim.length == nbDHosts;
            for (int j = 0; j < nbDHosts; j++) {
                dUses[j][d] = dUseDim[j];
            }
            d++;
        }
        IntVar[] earlyStarts = rp.getNodeActions().stream().map(NodeTransition::getHostingStart).toArray(IntVar[]::new);
        IntVar[] lastEnd = rp.getNodeActions().stream().map(NodeTransition::getHostingEnd).toArray(IntVar[]::new);
        rp.getModel().post(
                new TaskScheduler(earlyStarts,
                        lastEnd,
                        capas,
                        cHosts, cUses, cEnds,
                        dHosts, dUses, dStarts,
                        associations)
        );
        final List<KeepRunningVM> keepRunningVms = new ArrayList<>();
        final BitSet decreasing = new BitSet();
        int idx = 0;
        for (final VMTransition trans : rp.getVMActions()) {
            if (trans instanceof KeepRunningVM) {
                keepRunningVms.add((KeepRunningVM) trans);
                if (Boolean.TRUE.equals(strictlyDecreasingOrUnchanged(trans.getVM()))) {
                    decreasing.set(idx);
                }
                idx++;
            }
        }
        if (!keepRunningVms.isEmpty()) {
            StayingVMsScheduling.newConstraint(keepRunningVms.toArray(new KeepRunningVM[0]), decreasing).post();
        }
        return true;
    }

    private Boolean strictlyDecreasingOrUnchanged(VM vm) {
        //If it has non-overlapping slices
        int[] slicesIndexes = non.get(vm.id());
        if (slicesIndexes == null) {
            return false;
        }
        int dIdx = slicesIndexes[0];
        int cIdx = slicesIndexes[1];

        Boolean decOrStay = null;
        //Get the resources usage
        for (int d = 0; d < cUsages.size(); d++) {
            int req = dUsages.get(d)[dIdx];
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
}
