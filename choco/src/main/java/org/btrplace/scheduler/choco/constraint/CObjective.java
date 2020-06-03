/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

/**
 * Specify the choco implementation of an objective
 *
 * @author Fabien Hermenier
 */
public interface CObjective extends ChocoConstraint {

    /**
     * Post the constraints related to the objective.
     * This method is decoupled from {@link ChocoConstraint#inject(org.btrplace.scheduler.choco.Parameters, org.btrplace.scheduler.choco.ReconfigurationProblem)}
     * to allow to postpone the constraint posting to the last moment.
     */
    default void postCostConstraints() {
        //Do nothing by default
    }
}
