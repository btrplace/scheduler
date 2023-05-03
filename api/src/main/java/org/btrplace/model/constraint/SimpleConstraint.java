/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

/**
 * A skeleton for a constraint that can be either discrete or continuous.
 *
 * @author Fabien Hermenier
 */
public abstract class SimpleConstraint implements SatConstraint {

    private boolean continuous;

    /**
     * Build a new constraint.
     *
     * @param continuous {@code true} to state a continuous constraint. {@code false} for a discrete one
     */
    protected SimpleConstraint(boolean continuous) {
        this.continuous = continuous;
    }

    @Override
    public boolean isContinuous() {
        return continuous;
    }

    @Override
    public boolean setContinuous(boolean b) {
        this.continuous = b;
        return true;
    }
}
