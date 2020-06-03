/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.model.Model;
import org.btrplace.plan.event.EventCommittedListener;

/**
 * An object to simulate the application of
 * a plan. The result will be a new model.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationPlanApplier {

    /**
     * Add a listener that will be notified upon events termination.
     *
     * @param l the listener to add
     */
    void addEventCommittedListener(EventCommittedListener l);

    /**
     * Remove a listener.
     *
     * @param l the listener
     * @return {@code true} iff the listener is removed
     */
    boolean removeEventCommittedListener(EventCommittedListener l);

    /**
     * Apply a plan.
     *
     * @param p the plan to apply
     * @return the resulting model if the application succeed.
     */
    Model apply(ReconfigurationPlan p);

    /**
     * Textual representation of a plan.
     *
     * @param p the plan to stringify
     * @return the formatted string
     */
    String toString(ReconfigurationPlan p);
}
