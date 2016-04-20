/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
    public SimpleConstraint(boolean continuous) {
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
