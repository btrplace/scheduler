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

import org.chocosolver.solver.variables.IntVar;

/**
 * Interface to specify a multi-dimension cumulatives constraints.
 * Dimensions can be added on the fly.
 *
 * @author Fabien Hermenier
 */
public interface Cumulatives extends ChocoView {

    /**
     * View identifier.
     */
    String VIEW_ID = "choco.cumulatives";

    /**
     * Add a new dimension.
     *
     * @param c    the capacity of each node. The variables *must be* ordered according to {@link org.btrplace.scheduler.choco.DefaultReconfigurationProblem#getNode(org.btrplace.model.Node)}.
     * @param cUse the resource usage of each of the cSlices
     * @param dUse the resource usage of each of the dSlices
     */
    void addDim(IntVar[] c, int[] cUse, IntVar[] dUse);
}
