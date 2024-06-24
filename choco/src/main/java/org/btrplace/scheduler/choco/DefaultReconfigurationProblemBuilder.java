/*
 * Copyright  2024 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.scheduler.SchedulerException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Builder to help at the creation of a scheduler.
 * By default:
 * <ul>
 * <li>All the VMs are manageable</li>
 * <li>Default Parameters: {@link DefaultParameters}</li>
 * <li>The state of the VMs is unchanged</li>
 * </ul>
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationProblemBuilder {

    private final Model model;

    private Set<VM> runs;
    private Set<VM> waits;
    private Set<VM> over;
    private Set<VM> sleep;

    private Set<VM> manageable;

    private Set<VM> misplaced;

    private Parameters ps;

    /**
     * Make a new builder for a problem working on a given model.
     *
     * @param m the model to consider
     */
    public DefaultReconfigurationProblemBuilder(Model m) {
        model = m;
    }

    /**
     * Set the parameters to use.
     *
     * @param p the parameters
     * @return the current builder
     */
    public DefaultReconfigurationProblemBuilder setParams(Parameters p) {
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
     * Set the VMs that are considered to be misplaced.
     *
     * @param vms the set of VMs
     * @return the current builder
     */
    public DefaultReconfigurationProblemBuilder setMisplacedVMs(Set<VM> vms) {
        misplaced = vms;
        return this;
    }

    /**
     * Build the problem
     *
     * @return the builder problem
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred
     */
    public DefaultReconfigurationProblem build() throws SchedulerException {

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

        if (misplaced == null) {
            misplaced = new HashSet<>();
        }
        if (ps == null) {
            ps = new DefaultParameters();
        }
        return new DefaultReconfigurationProblem(model, ps, waits, runs, sleep, over, manageable, misplaced);
    }

}
