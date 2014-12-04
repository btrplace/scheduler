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
 * An abstract constraint to create vector packing constraints.
 * For performance reason, it is possible to consider multiple dimensions in a single constraint
 *
 * @author Fabien Hermenier
 */
public abstract class Packing implements ChocoView {

    /**
     * The view identifier.
     */
    public static final String VIEW_ID = "choco.packing";

    @Override
    public String getIdentifier() {
        return VIEW_ID;
    }

    /**
     * Add a new dimension.
     *
     * @param name the dimension label
     * @param l    the load of each VM. The variables *must be* ordered according to {@link org.btrplace.scheduler.choco.DefaultReconfigurationProblem#getVM(org.btrplace.model.VM)}.
     * @param s    the capacity of each node. The variables *must be* ordered according to {@link org.btrplace.scheduler.choco.DefaultReconfigurationProblem#getNode(org.btrplace.model.Node)}.
     * @param b    the placement variable for each VM. Same order than for {@code l}
     */
    public abstract void addDim(String name, IntVar[] l, IntVar[] s, IntVar[] b);
}
