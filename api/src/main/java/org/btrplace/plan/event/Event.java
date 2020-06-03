/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;

import org.btrplace.model.Model;

/**
 * A event to apply on a model to modify it.
 * See the {@link Action} class for a time-bounded event.
 *
 * @author Fabien Hermenier
 * @see Action
 */
public interface Event {

    /**
     * Apply the event on a given model.
     *
     * @param m the model to modify
     * @return {@code true} iff the modification succeeded
     */
    boolean apply(Model m);


    /**
     * Notify a visitor to visit the action.
     *
     * @param v the visitor to notify
     * @return the visit result
     */
    Object visit(ActionVisitor v);
}
