/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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

import gnu.trove.list.array.TIntArrayList;
import org.btrplace.model.VM;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.extensions.AliasedCumulatives;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * Make a new builder.
     *
     * @param p the associated problem
     */
    public DefaultAliasedCumulatives(ReconfigurationProblem p) {
        super(p);
        capacities = new TIntArrayList();
        aliases = new ArrayList<>();

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
        for (int i = 0; i < aliases.size(); i++) {
            int capa = capacities.get(i);
            int[] alias = aliases.get(i);
            int[] cUse = cUsages.get(i);
            int[] dUses = new int[dUsages.get(i).length];
            for (IntVar dUseDim : dUsages.get(i)) {
                dUses[i++] = dUseDim.getLB();
            }
            rp.getSolver().post(new AliasedCumulatives(alias,
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
    public boolean insertActions(ReconfigurationProblem r, ReconfigurationPlan p) {
        return true;
    }

    @Override
    public boolean cloneVM(VM vm, VM clone) {
        return true;
    }

    /**
     * Builder associated to this constraint.
     */
    public static class Builder extends SolverViewBuilder {

        @Override
        public String getKey() {
            return org.btrplace.scheduler.choco.view.AliasedCumulatives.VIEW_ID;
        }

        @Override
        public org.btrplace.scheduler.choco.view.AliasedCumulatives build(ReconfigurationProblem p) {
            return new DefaultAliasedCumulatives(p);
        }

        @Override
        public List<String> getDependencies() {
            return Collections.emptyList();
        }

    }
}
