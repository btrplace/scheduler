/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.transition;

import org.btrplace.plan.ReconfigurationPlan;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

/**
 * A model skeleton for a transition.
 *
 * @author Fabien Hermenier
 */
public interface Transition<E extends Enum<E>> {

    /**
     * Get the moment the action starts.
     *
     * @return a variable that must be positive
     */
    IntVar getStart();

    /**
     * Get the moment the action ends.
     *
     * @return a variable that must be greater than {@link #getStart()}
     */
    IntVar getEnd();

    /**
     * Get the action duration.
     *
     * @return a duration equals to {@code getEnd() - getStart()}
     */
    IntVar getDuration();

    /**
     * Insert into a plan the actions resulting from the model.
     * The variable values must be extracted from the solution object {@code s} and not directly.
     * @param s the solution computed by the solver.
     * @param plan the plan to modify
     * @return {@code true} iff success
     */
    boolean insertActions(Solution s, ReconfigurationPlan plan);

    /**
     * Get the next state of the subject manipulated by the action.
     *
     * @return {@code 0} for offline, {@code 1} for online.
     */
    BoolVar getState();
}
