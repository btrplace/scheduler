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

import org.btrplace.model.VM;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation that relies on the pack constraint bundled with Choco.
 * This is a pretty slow constraint... but it exists.
 *
 * @author Fabien Hermenier
 */
public class DefaultPacking extends Packing {

    private ReconfigurationProblem rp;

    private List<IntVar[]> loads;

    private List<IntVar[]> bins;

    private List<IntVar[]> sizes;

    private List<String> names;

    /**
     * A new constraint.
     *
     * @param p the associated problem
     */
    public DefaultPacking(ReconfigurationProblem p) {
        loads = new ArrayList<>();
        bins = new ArrayList<>();
        sizes = new ArrayList<>();
        names = new ArrayList<>();
        this.rp = p;

    }

    @Override
    public void addDim(String name, IntVar[] l, IntVar[] s, IntVar[] b) {
        this.loads.add(l);
        this.sizes.add(s);
        this.bins.add(b);
        this.names.add(name);
    }

    @Override
    public boolean beforeSolve(ReconfigurationProblem p) {
        Solver solver = rp.getSolver();
        int[][] iSizes = new int[sizes.size()][sizes.get(0).length];
        for (int i = 0; i < sizes.size(); i++) {
            IntVar[] s = sizes.get(i);
            int x = 0;
            for (IntVar ss : s) {
                iSizes[i][x++] = ss.getLB();
                try {
                    ss.instantiateTo(ss.getLB(), Cause.Null);
                } catch (ContradictionException ex) {
                    rp.getLogger().error("Unable post the packing constraint for dimension '{}': ", names.get(i));
                    return false;
                }
            }
            if (!rp.getFutureRunningVMs().isEmpty()) {
                solver.post(IntConstraintFactory.bin_packing(bins.get(0), iSizes[i], loads.get(i), 0));
            }
        }
        return true;
    }

    @Override
    public boolean insertActions(ReconfigurationProblem pb, ReconfigurationPlan p) {
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
            return Packing.VIEW_ID;
        }

        @Override
        public Packing build(ReconfigurationProblem p) {
            return new DefaultPacking(p);
        }

        @Override
        public List<String> getDependencies() {
            return Collections.emptyList();
        }

    }
}
