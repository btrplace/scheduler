/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.model.Model;
import org.btrplace.plan.event.Action;

/**
 * An exception to notify an action is not feasible due to the current model state.
 *
 * @author Fabien Hermenier
 */
public class InfeasibleActionException extends RuntimeException {

    private final transient Model model;

    private final transient Action action;

    /**
     * New exception.
     *
     * @param model  the initial model
     * @param action the action that cannot be applied on the model
     */
    public InfeasibleActionException(Model model, Action action) {
        super("Action '" + action + "' cannot be applied on the following mode:\n" + model);
        this.action = action;
        this.model = model;
    }

    /**
     * Get the initial model.
     *
     * @return a model
     */
    public Model getModel() {
        return model;
    }

    /**
     * Get the action that is not applyable
     *
     * @return an action
     */
    public Action getAction() {
        return action;
    }
}
