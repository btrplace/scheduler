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
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/*
 * Created on 17/09/14.
 *
 * @author Sophie Demassey
 */
public class VectorPacking extends Packing {

    private ReconfigurationProblem rp;

    private List<IntVar[]> loads;

    private List<IntVar[]> bins;

    private List<IntVar[]> sizes;

    private List<String> names;

    private int dim;

    /**
     * A new constraint.
     *
     * @param p the associated problem
     */
    public VectorPacking(ReconfigurationProblem p) {
        loads = new ArrayList<>();
        bins = new ArrayList<>();
        sizes = new ArrayList<>();
        names = new ArrayList<>();
        this.rp = p;
        dim = 0;
    }

    @Override
    public void addDim(String name, IntVar[] l, IntVar[] s, IntVar[] b) {
        this.loads.add(l);
        this.sizes.add(s);
        this.bins.add(b);
        this.names.add(name);
        this.dim++;
    }

    @Override
    public boolean beforeSolve(ReconfigurationProblem p) {
        Solver solver = rp.getSolver();
        int[][] aSizes = new int[dim][sizes.get(0).length];
        IntVar[][] aLoads = new IntVar[dim][];
        String[] aNames = new String[dim];
        for (int d = 0; d < dim; d++) {
            aLoads[d] = Arrays.copyOf(loads.get(d), loads.get(d).length);
            assert bins.get(d).length == 0 || bins.get(d)[0].equals(bins.get(0)[0]);
            aNames[d] = names.get(d);
            IntVar[] s = sizes.get(d);
            int x = 0;
            for (IntVar ss : s) {
                aSizes[d][x++] = ss.getLB();
                try {
                    ss.instantiateTo(ss.getLB(), Cause.Null);
                } catch (ContradictionException ex) {
                    rp.getLogger().error("Unable post the vector packing constraint");
                    return false;
                }
            }
        }
        if (!rp.getFutureRunningVMs().isEmpty()) {
            solver.post(new org.btrplace.scheduler.choco.extensions.pack.VectorPacking(aNames, aLoads, aSizes, bins.get(0), true, true));
            //IntConstraintFactory.bin_packing(bins.get(0), iSizes[i], loads.get(i), 0));
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
            return new VectorPacking(p);
        }

        @Override
        public List<String> getDependencies() {
            return Collections.emptyList();
        }

    }
}
