/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
