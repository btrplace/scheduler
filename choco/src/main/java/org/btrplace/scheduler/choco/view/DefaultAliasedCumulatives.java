/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.view;

import gnu.trove.list.array.TIntArrayList;
import org.btrplace.model.VM;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.extensions.AliasedCumulatives;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder to create constraints where slices have to be placed on nodes
 * with regards to the slice and the nodes capacity.
 * <p>
 * It differs from {@link org.btrplace.scheduler.choco.view.DefaultCumulatives} as
 * a resource may in fact be an alias to another one. This allows
 * to create a fake resource that aggregate the capacity of each of
 * the aliased resources.
 *
 * @author Fabien Hermenier
 */
public class DefaultAliasedCumulatives extends AbstractCumulatives implements org.btrplace.scheduler.choco.view.AliasedCumulatives {

    private TIntArrayList capacities;

    private List<int[]> aliases;

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        super.inject(ps, rp);
        capacities = new TIntArrayList();
        aliases = new ArrayList<>();

        return true;
    }

    /**
     * Add a constraint
     *
     * @param c     the cumulative capacity of the aliased resources
     * @param cUse  the usage of each of the c-slices
     * @param dUse  the usage of each of the d-slices
     * @param alias the resource identifiers that compose the alias
     */
    @Override
    public void addDim(int c, int[] cUse, IntVar[] dUse, int[] alias) {
        capacities.add(c);
        cUsages.add(cUse);
        dUsages.add(dUse);
        aliases.add(alias);
    }

    /**
     * Get the generated constraints.
     *
     * @return a list of constraint that may be empty.
     */
    @Override
    public boolean beforeSolve(ReconfigurationProblem r) {
        super.beforeSolve(r);
        for (int i = 0; i < aliases.size(); i++) {
            int capa = capacities.get(i);
            int[] alias = aliases.get(i);
            int[] cUse = cUsages.get(i);
            int[] dUses = new int[dUsages.get(i).length];
            for (IntVar dUseDim : dUsages.get(i)) {
                dUses[i++] = dUseDim.getLB();
            }
            r.getModel().post(new AliasedCumulatives(alias,
                    new int[]{capa},
                    cHosts, new int[][]{cUse}, cEnds,
                    dHosts, new int[][]{dUses}, dStarts,
                    associations));

        }
        return true;
    }

    @Override
    public String getIdentifier() {
        return org.btrplace.scheduler.choco.view.AliasedCumulatives.VIEW_ID;
    }

    @Override
    public boolean insertActions(ReconfigurationProblem r, Solution s, ReconfigurationPlan p) {
        return true;
    }

    @Override
    public boolean cloneVM(VM vm, VM clone) {
        return true;
    }
}
