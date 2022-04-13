/*
 * Copyright  2022 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.view;

import org.btrplace.model.Instance;
import org.btrplace.model.VM;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Injectable;
import org.btrplace.scheduler.choco.MisplacedVMsEstimator;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.chocosolver.solver.Solution;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Interface denoting the Choco implementation of a View. Such a view will be automatically
 * instantiated from {@link org.btrplace.scheduler.choco.constraint.ChocoMapper} using
 * the api-side view provided as a parameter or using a default constructor if this class
 * is a solver-only view provided by {@link Parameters#getChocoViews()}.
 *
 * @author Fabien Hermenier
 */
public interface ChocoView extends Injectable, MisplacedVMsEstimator {

    /**
     * Get the view unique identifier.
     *
     * @return a non-empty String
     */
    String getIdentifier();

    /**
     * An event that is send to indicate a RP will be solved.
     * The view can then customize the RP a last time.
     *
     * @param rp the problem we solve
     * @return {@code false} iff there will be no solution to the RP.
     * @throws SchedulerException if an error occurred while building the problem
     */
    default boolean beforeSolve(@SuppressWarnings("unused") ReconfigurationProblem rp) throws SchedulerException {
        return true;
    }

    /**
     * Allow the insertion of actions on the plan computed for a given problem.
     *
     * The variable values must be extracted from the solution object {@code s} and not directly.
     * @param rp the solver problem
     * @param s the solution computed by the solver.
     * @param p  the computed plan
     * @return {@code true} iff the insertion succeeded
     */
    default boolean insertActions(ReconfigurationProblem rp, Solution s, ReconfigurationPlan p) {
        return true;
    }

    /**
     * Notify a new VM will be a clone of an already known VM.
     *
     * @param vm    the old VM that will be substituted by the clone
     * @param clone the clone identifier
     * @return {@code true} iff the view validate the cloning process.
     */
    default boolean cloneVM(VM vm, VM clone) {
        return true;
    }

    /**
     * Get the view dependencies.
     * The dependencies will be injected in prior.
     *
     * @param rp the problem to solve
     * @return a list of view identifiers that may be empty
     */
    default List<String> getDependencies(ReconfigurationProblem rp) {
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     *
     * @param i the model to use to inspect the VMs.
     * @return all the model VMs.
     */
    @Override
    default Set<VM> getMisPlacedVMs(Instance i) {
        return Collections.emptySet();
    }
}
