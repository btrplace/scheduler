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

import btrplace.solver.choco.chocoUtil.LightBinPacking;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder to create {@link btrplace.solver.choco.chocoUtil.LightBinPacking} constraints
 *
 * @author Fabien Hermenier
 */
public class BinPackingBuilder {

    private ReconfigurationProblem rp;

    private List<IntDomainVar[]> loads;

    private List<IntDomainVar[]> bins;

    private List<IntDomainVar[]> sizes;

    /**
     * Make a new builder.
     *
     * @param rp the associated problem
     */
    public BinPackingBuilder(ReconfigurationProblem rp) {
        this.rp = rp;
        loads = new ArrayList<IntDomainVar[]>();
        bins = new ArrayList<IntDomainVar[]>();
        sizes = new ArrayList<IntDomainVar[]>();

    }

    /**
     * Add a dimension.
     *
     * @param loads the resource capacity of each of the nodes
     * @param sizes the resource usage of each of the cSlices
     * @param bins  the resource usage of each of the dSlices
     */
    public void add(IntDomainVar[] loads, IntDomainVar[] sizes, IntDomainVar[] bins) {
        this.loads.add(loads);
        this.sizes.add(sizes);
        this.bins.add(bins);
    }

    /**
     * Build the constraint.
     *
     * @return the resulting constraint
     */
    public void inject() throws ContradictionException {
        CPSolver solver = rp.getSolver();


        for (int i = 0; i < loads.size(); i++) {
            IntDomainVar[] l = loads.get(i);
            IntDomainVar[] s = sizes.get(i);
            IntDomainVar[] b = bins.get(i);

            //Instantiate the item sizes to their LB
            for (IntDomainVar ss : s) {
                ss.setVal(ss.getInf());
            }
            solver.post(new LightBinPacking(solver.getEnvironment(), l, s, b));
        }


    }
}
