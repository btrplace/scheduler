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

package btrplace.solver.choco;

import solver.Cause;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder to create {@link btrplace.solver.choco.extensions.LightBinPacking} constraints
 *
 * @author Fabien Hermenier
 */
public class BinPackingBuilder {

    private ReconfigurationProblem rp;

    private List<IntVar[]> loads;

    private List<IntVar[]> bins;

    private List<IntVar[]> sizes;

    private List<String> names;

    /**
     * Make a new builder.
     *
     * @param p the associated problem
     */
    public BinPackingBuilder(ReconfigurationProblem p) {
        this.rp = p;
        loads = new ArrayList<>();
        bins = new ArrayList<>();
        sizes = new ArrayList<>();
        names = new ArrayList<>();

    }

    /**
     * Add a dimension.
     *
     * @param l the resource capacity of each of the nodes
     * @param s the resource usage of each of the cSlices
     * @param b the resource usage of each of the dSlices
     */
    public void add(String name, IntVar[] l, IntVar[] s, IntVar[] b) {
        this.loads.add(l);
        this.sizes.add(s);
        this.bins.add(b);
        this.names.add(name);
    }

    /**
     * Build the constraint.
     */
    public void inject() throws ContradictionException {
        Solver solver = rp.getSolver();
        int[][] iSizes = new int[sizes.size()][sizes.get(0).length];
        for (int i = 0; i < sizes.size(); i++) {
            IntVar[] s = sizes.get(i);
            int x = 0;
            for (IntVar ss : s) {
                iSizes[i][x++] = ss.getLB();
                ss.instantiateTo(ss.getLB(), Cause.Null);
            }
            //assign, size, load, offset ,
            if (!rp.getFutureRunningVMs().isEmpty()) {
                solver.post(IntConstraintFactory.bin_packing(bins.get(0), iSizes[i], loads.get(i), 0));
            }
        }
        //TODO: Items must always be in the same order.
        //solver.post(new LightBinPacking(names.toArray(new String[names.size()]), solver.getEnvironment(), loads.toArray(new IntVar[loads.size()][]), iSizes, bins.get(0)));
    }
}
