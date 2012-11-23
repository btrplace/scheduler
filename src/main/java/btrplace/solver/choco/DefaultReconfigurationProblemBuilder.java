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

package btrplace.solver.choco;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.plan.SolverException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Builder to help at the creation of a reconfiguration algorithm.
 * By default, variables are not labelled to save memory, all the VMs are manageable and they stay in their current state.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationProblemBuilder {

    private Model model;

    private boolean labelVars = false;

    private DurationEvaluators dEval;

    private Set<UUID> runs, waits, over, sleep;
    private Set<UUID> manageable;

    /**
     * Make a new builder for a problem working on a given model.
     *
     * @param m the model to consider
     */
    public DefaultReconfigurationProblemBuilder(Model m) {
        model = m;
    }

    /**
     * Label the variables created by the problem.
     *
     * @return the current builder
     */
    public DefaultReconfigurationProblemBuilder labelVariables() {
        labelVars = true;
        return this;
    }

    /**
     * Provide a dedicated {@link DurationEvaluators}.
     *
     * @param d the evaluator to use
     * @return the current builder
     */
    public DefaultReconfigurationProblemBuilder setDurationEvaluatators(DurationEvaluators d) {
        dEval = d;
        return this;
    }

    /**
     * Set the next state of the VMs.
     * Sets must be disjoint
     *
     * @param waiting    the future VMs in the waiting state
     * @param runnings   the future VMs in the running state
     * @param sleepings  the future VMs in the sleeping state
     * @param terminated the VMs to terminate
     * @return the current builder
     */
    public DefaultReconfigurationProblemBuilder setNextVMsStates(Set<UUID> waiting,
                                                                 Set<UUID> runnings,
                                                                 Set<UUID> sleepings,
                                                                 Set<UUID> terminated) {
        runs = runnings;
        waits = waiting;
        sleep = sleepings;
        over = terminated;
        return this;
    }

    /**
     * Set the VMs that are manageable by the problem.
     *
     * @param vms the set of VMs
     * @return the current builder
     */
    public DefaultReconfigurationProblemBuilder setManageableVMs(Set<UUID> vms) {
        manageable = vms;
        return this;
    }

    /**
     * Build the problem
     *
     * @return the builder problem
     * @throws SolverException if an error occurred
     */
    public DefaultReconfigurationProblem build() throws SolverException {
        if (runs == null) { //The others are supposed to be null to
            Mapping map = model.getMapping();
            runs = map.getRunningVMs();
            waits = map.getWaitingVMs();
            sleep = map.getSleepingVMs();
            over = new HashSet<UUID>();
        }
        if (dEval == null) {
            dEval = new DurationEvaluators();
        }
        if (manageable == null) {
            manageable = new HashSet<UUID>();
            manageable.addAll(runs);
            manageable.addAll(waits);
            manageable.addAll(sleep);
            manageable.addAll(over);
        }

        return new DefaultReconfigurationProblem(model, dEval, waits, runs, sleep, over, manageable, labelVars);
    }

}
