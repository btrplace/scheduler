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

/**
 * Different classes to model the possible state transitions for a VM or a node.
 * The most common entry point is {@link org.btrplace.scheduler.choco.transition.TransitionFactory} that
 * will pick up the right model according to the initial and the next state of an element.
 *
 * Once the associated {@link org.btrplace.scheduler.choco.ReconfigurationProblem} solved,
 * the associated transitions will generate {@link org.btrplace.plan.event.Action}.
 * @see org.btrplace.scheduler.choco.transition.Transition
 * @see org.btrplace.scheduler.choco.transition.TransitionFactory
 */
package org.btrplace.scheduler.choco.transition;
