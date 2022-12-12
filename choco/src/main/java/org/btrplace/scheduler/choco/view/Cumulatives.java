/*
 * Copyright  2022 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.view;

import org.chocosolver.solver.variables.IntVar;

import java.util.List;

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
     * @param dUse the resource demand of each of the dSlices
     */
    void addDim(List<IntVar> c, int[] cUse, int[] dUse);

    @Override
    default String getIdentifier() {
        return VIEW_ID;
    }
}
