/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.durationEvaluator;

import btrplace.model.IntResource;
import btrplace.solver.choco.DurationEvaluator;

import java.util.UUID;

/**
 * Evaluate the duration of an action on an element
 * linearly from a given resource. The duration {@code d} is expressed as {@code d = a * rc.get(e) + b}.
 *
 * @author Fabien Hermenier
 */
public class LinearToAResourceDuration implements DurationEvaluator {

    private IntResource rc;

    private int a;

    private int b;

    /**
     * Make a new evaluator.
     *
     * @param rc the resource to consider
     * @param a  the coefficient
     */
    public LinearToAResourceDuration(IntResource rc, int a) {
        this(rc, a, 0);
    }

    /**
     * Make a new evaluator.
     *
     * @param rc the resource to consider
     * @param a  the coefficient
     * @param b  the initial value
     */
    public LinearToAResourceDuration(IntResource rc, int a, int b) {
        this.rc = rc;
        this.a = a;
        this.b = b;
    }

    @Override
    public int evaluate(UUID e) {
        int x = rc.get(e);
        return a * x + b;
    }
}
