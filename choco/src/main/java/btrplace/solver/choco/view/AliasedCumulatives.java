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

import solver.variables.IntVar;

/**
 * An interface to specify a cumulatives constraint when a resource is shared among
 * multiple nodes.
 * @author Fabien Hermenier
 */
public interface AliasedCumulatives extends ChocoModelView {

    /**
     * The view identifier.
     */
    final String VIEW_ID = "choco.aliasedCumulatives";

    /**
     * Add a new dimension.
     * @param c the capacity of the resource
     * @param cUse the current usage for each VM
     * @param dUse the current demain for each VM
     * @param alias the indexes of the nodes that share the resource
     */
    void addDim(int c, int[] cUse, IntVar[] dUse, int[] alias);
}
