/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.view;

import org.chocosolver.solver.variables.IntVar;

/**
 * An interface to specify a cumulatives constraint when a resource is shared among
 * multiple nodes.
 *
 * @author Fabien Hermenier
 */
public interface AliasedCumulatives extends ChocoView {

    /**
     * The view identifier.
     */
    String VIEW_ID = "choco.aliasedCumulatives";

    /**
     * Add a new dimension.
     *
     * @param c     the capacity of the resource
     * @param cUse  the current usage for each VM
     * @param dUse  the current demain for each VM
     * @param alias the indexes of the nodes that share the resource
     */
    void addDim(int c, int[] cUse, IntVar[] dUse, int[] alias);
}
