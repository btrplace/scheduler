/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

/**
 * Optimization oriented constraint.
 * Such a constraint cannot be violated. It just asks to minimize or minimize
 * a value.
 *
 * @author Fabien Hermenier
 */
public abstract class OptConstraint implements Constraint {

    /**
     * Get the constraint identifier.
     *
     * @return a non-empty String
     */
    public abstract String id();

    @Override
    public int hashCode() {
        return id().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && this.getClass().equals(obj.getClass());
    }

    @Override
    public String toString() {
        return id();
    }
}
