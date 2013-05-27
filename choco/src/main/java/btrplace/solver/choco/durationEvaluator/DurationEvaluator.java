/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

import btrplace.model.Model;


/**
 * Interface to specify the duration evaluator for a possible action on an element.
 *
 * @author Fabien Hermenier
 */
public interface DurationEvaluator {

    /**
     * Evaluate the duration of the action on a given element.
     *
     * @param mo the model to consider
     * @param e  the element
     * @return a positive integer
     */
    int evaluate(Model mo, int e);
}
