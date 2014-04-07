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

import btrplace.solver.SolverException;
import solver.variables.IntVar;

/**
 * An abstract constraint to create vector packing constraints.
 * For performance reason, it is possible to consider multiple dimensions in a single constraint
 *
 * @author Fabien Hermenier
 */
public interface PackingConstraint {


    /**
     * Add a new dimension.
     *
     * @param name the dimension label
     * @param l    the load of each VM. The variables *must be* ordered according to {@link btrplace.solver.choco.DefaultReconfigurationProblem#getVM(btrplace.model.VM)}.
     * @param s    the capacity of each node. The variables *must be* ordered according to {@link btrplace.solver.choco.DefaultReconfigurationProblem#getVM(btrplace.model.VM)}.
     * @param b    the placement variable for each VM. Same order than for {@code l}
     */
    void addDim(String name, IntVar[] l, IntVar[] s, IntVar[] b);

    /**
     * Commit all the stated dimensions, generate then plug the underlying constraints to the problem
     *
     * @return {@code false} if the resulting problem has no solution
     * @throws SolverException if an error occurred while building the underlying constraints.
     */
    boolean commit() throws SolverException;
}
