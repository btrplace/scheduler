/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco.constraint;

/**
 * Specify the choco implementation of an objective
 *
 * @author Fabien Hermenier
 */
public interface CObjective extends ChocoConstraint {

    /**
     * Post the constraints related to the objective.
     * This method is decoupled from {@link #inject(org.btrplace.scheduler.choco.ReconfigurationProblem)}
     * to allow to postpone the constraint posting to the last moment.
     */
    void postCostConstraints();
}
