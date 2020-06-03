/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.event.Action;

/**
 * An exception to signal an action violates a given constraint.
 *
 * @author Fabien Hermenier
 */
public class ContinuousViolationException extends SatConstraintViolationException {

    private final transient Action action;

    /**
     * New constraint.
     *
     * @param cstr   the violated constraint
     * @param action the action that violates the constraint
     */
    public ContinuousViolationException(SatConstraint cstr, Action action) {
        super(cstr, "Constraint '" + cstr + "' is violated by the action '" + action + "'");
        this.action = action;
    }

    public Action getAction() {
        return action;
    }
}
