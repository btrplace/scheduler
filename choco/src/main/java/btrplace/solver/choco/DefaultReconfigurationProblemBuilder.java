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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Builder to help at the creation of a reconfiguration algorithm.
 * By default:
 * <ul>
 * <li>Variables are not labelled to save memory</li>
 * <li>All the VMs are manageable</li>
 * <li>Default ChocoReconfigurationAlgorithmParams: {@link btrplace.solver.choco.DefaultChocoReconfigurationAlgorithmParams}</li>
 * <li>The state of the VMs is unchanged</li>
 * </ul>
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationProblemBuilder {

    private Model model;

    private boolean labelVars = false;

    private Set<VM> runs, waits, over, sleep;

    private Set<VM> manageable;

    private ChocoReconfigurationAlgorithmParams ps;

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
     * Set the parameters to use.
     *
     * @param p the parameters
     * @return the current builder
     */
    public DefaultReconfigurationProblemBuilder setParams(ChocoReconfigurationAlgorithmParams p) {
        this.ps = p;
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

        if (manageable == null) {
            manageable = new HashSet<>();
            manageable.addAll(model.getMapping().getSleepingVMs());
            manageable.addAll(model.getMapping().getRunningVMs());
            manageable.addAll(model.getMapping().getReadyVMs());
        }

        if (ps == null) {
            ps = new DefaultChocoReconfigurationAlgorithmParams();
        }
        return new DefaultReconfigurationProblem(model, ps, waits, runs, sleep, over, manageable, labelVars);
    }

}
