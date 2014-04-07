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

package btrplace.solver.choco;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.duration.DurationEvaluators;
import btrplace.solver.choco.transition.TransitionFactory;
import btrplace.solver.choco.view.ModelViewMapper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Builder to help at the creation of a reconfiguration algorithm.
 * By default:
 * <ul>
 * <li>Variables are not labelled to save memory</li>
 * <li>All the VMs are manageable</li>
 * <li>Default DurationEvaluators: {@link btrplace.solver.choco.duration.DurationEvaluators#newBundle()}</li>
 * <li>Default ViewMapper: {@link btrplace.solver.choco.view.ModelViewMapper#newBundle()}</li>
 * <li>Default TransitionFactory: {@link btrplace.solver.choco.transition.TransitionFactory#newBundle()}</li>
 * <li>Default PackingConstraint: {@link DefaultPackingConstraint}</li>
 * <li>The state of the VMs is unchanged</li>
 * </ul>
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationProblemBuilder {

    private Model model;

    private boolean labelVars = false;

    private DurationEvaluators dEval;

    private ModelViewMapper viewMapper;

    private Set<VM> runs, waits, over, sleep;

    private Set<VM> manageable;

    private TransitionFactory amf;

    private PackingConstraintBuilder packBuilder;

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
        return labelVariables(true);
    }

    /**
     * Label the variables created by the problem.
     *
     * @param b {@code true} to label the variables
     * @return the current builder
     */
    public DefaultReconfigurationProblemBuilder labelVariables(boolean b) {
        labelVars = b;
        return this;
    }


    /**
     * Provide a dedicated {@link DurationEvaluators}.
     *
     * @param d the evaluator to use
     * @return the current builder
     */
    public DefaultReconfigurationProblemBuilder setDurationEvaluators(DurationEvaluators d) {
        dEval = d;
        return this;
    }

    public DefaultReconfigurationProblemBuilder setPackingBuilder(PackingConstraintBuilder p) {
        packBuilder = p;
        return this;
    }
    /**
     * Provide a dedicated {@link btrplace.solver.choco.transition.TransitionFactory}.
     *
     * @param a the factory to use
     * @return the current builder
     */
    public DefaultReconfigurationProblemBuilder setTransitionFactory(TransitionFactory a) {
        amf = a;
        return this;
    }

    /**
     * Provide a dedicated {@link ModelViewMapper}.
     *
     * @param m the mapper to use
     * @return the current builder
     */
    public DefaultReconfigurationProblemBuilder setViewMapper(ModelViewMapper m) {
        viewMapper = m;
        return this;
    }

    /**
     * Set the next state of the VMs.
     * Sets must be disjoint
     *
     * @param ready    the future VMs in the ready state
     * @param running  the future VMs in the running state
     * @param sleeping the future VMs in the sleeping state
     * @param killed   the VMs to kill
     * @return the current builder
     */
    public DefaultReconfigurationProblemBuilder setNextVMsStates(Set<VM> ready,
                                                                 Set<VM> running,
                                                                 Set<VM> sleeping,
                                                                 Set<VM> killed) {
        runs = running;
        waits = ready;
        sleep = sleeping;
        over = killed;
        return this;
    }

    /**
     * Set the VMs that are manageable by the problem.
     *
     * @param vms the set of VMs
     * @return the current builder
     */
    public DefaultReconfigurationProblemBuilder setManageableVMs(Set<VM> vms) {
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

        if (runs == null) {
            //The others are supposed to be null too as they are set using the same method
            Mapping map = model.getMapping();
            runs = new HashSet<>();
            sleep = new HashSet<>();
            for (Node n : map.getOnlineNodes()) {
                runs.addAll(map.getRunningVMs(n));
                sleep.addAll(map.getSleepingVMs(n));
            }
            waits = map.getReadyVMs();
            over = Collections.emptySet();
        }
        if (dEval == null) {
            dEval = DurationEvaluators.newBundle();
        }
        if (viewMapper == null) {
            viewMapper = ModelViewMapper.newBundle();
        }
        if (manageable == null) {
            manageable = new HashSet<>();
            manageable.addAll(model.getMapping().getSleepingVMs());
            manageable.addAll(model.getMapping().getRunningVMs());
            manageable.addAll(model.getMapping().getReadyVMs());
        }
        if (amf == null) {
            amf = TransitionFactory.newBundle();
        }

        if (packBuilder == null) {
            packBuilder = new DefaultPackingConstraint.Builder();
        }
        return new DefaultReconfigurationProblem(model, dEval, viewMapper, amf, packBuilder, waits, runs, sleep, over, manageable, labelVars);
    }

}
