/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.view;

import org.chocosolver.solver.variables.IntVar;

import java.util.List;

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
     * @param l    the capacity of each node. The variables *must be* ordered according to {@link org.btrplace.scheduler.choco.DefaultReconfigurationProblem#getNode(org.btrplace.model.Node)}.
     * @param s    The VM consumption
     * @param b    the placement variable for each VM. Same order than for {@code s}
     */
    public abstract void addDim(String name, List<IntVar> l, int[] s, IntVar[] b);
}
