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

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Interface for the Reconfiguration Problem.
 * @author Fabien Hermenier
 */
public interface ReconfigurationProblem {

    /**
     * The maximum duration of a plan in seconds: One hour.
     */
    Integer DEFAULT_MAX_TIME = 3600;

    /**
     * Get the current location of a running or a sleeping VM.
     *
     * @param vmIdx the index of the virtual machine
     * @return the node index if exists or -1 if the VM is not already placed
     */
    int getCurrentVMLocation(int vmIdx);

    /**
     * Get all the nodes in the model. Indexed by their identifier.
     *
     * @return an array of node.
     */
    UUID[] getNodes();

    /**
     * Get all the virtual machines in the model. Indexed by their identifier.
     *
     * @return an array of virtual machines.
     */
    UUID[] getVirtualMachines();

    /**
     * Get the initial Model.
     *
     * @return a model
     */
    Model getSourceModel();

    /**
     * Get the virtual machines that will be in the running state at the
     * end of the reconfiguration process.
     *
     * @return a set, may be empty
     */
    Set<UUID> getFutureRunnings();

    /**
     * Get the virtual machines that will be in the waiting state at the
     * end of the reconfiguration process.
     *
     * @return a set, may be empty
     */
    Set<UUID> getFutureWaitings();

    /**
     * Get the virtual machines that will be in the sleeping state at the
     * end of the reconfiguration process.
     *
     * @return a set, may be empty
     */
    Set<UUID> getFutureSleepings();

    /**
     * Get the virtual machines that will be in the terminated state at the
     * end of the reconfiguration process.
     *
     * @return a set, may be empty
     */
    Set<UUID> getFutureTerminated();

    /**
     * Get the nodes that will be in the online state at the
     * end of the reconfiguration process.
     *
     * @return a set, may be empty
     */
    Set<UUID> getFutureOnlines();

    /**
     * Get the nodes that will be in the offline state at the
     * end of the reconfiguration process.
     *
     * @return a set, may be empty
     */
    Set<UUID> getFutureOfflines();

    /**
     * Get the starting moment of the reconfiguration.
     *
     * @return a variable equals to 0
     */
    IntDomainVar getStart();

    /**
     * Get the end  moment of the reconfiguration
     *
     * @return a variable, should be equals to the last end moment of actions
     */
    IntDomainVar getEnd();

    /**
     * Get the index of a virtual machine
     *
     * @param vm the virtual machine
     * @return its index or -1 in case of failure
     */
    int getVM(UUID vm);

    /**
     * Get the virtual machine with a specified index
     *
     * @param idx the index of the virtual machine
     * @return the virtual machine or null in case of failure
     */
    UUID getVM(int idx);

    /**
     * Get the index of a node
     *
     * @param n the node
     * @return its index or -1 in case of failure
     */
    int getNode(UUID n);

    /**
     * Get the node with a specified index
     *
     * @param idx the index of the node
     * @return the node or null in case of failure
     */
    UUID getNode(int idx);

    /**
     * Get all the actions related to virtual machines.
     *
     * @return a list of actions.
     */
    List<ActionModel> getVMActions();

    /**
     * Get the action associated to a virtual machine.
     *
     * @param vm the virtual machine
     * @return the action associated to the virtual machine.
     */
    ActionModel getVMAction(UUID vm);

    /**
     * Get the action associated to a virtual machine.
     *
     * @param vmIdx the index of the virtual machine
     * @return the action associated to the virtual machine.
     */
    ActionModel getVMAction(int vmIdx);

    /**
     * Get all the actions related to nodes.
     *
     * @return a list of actions.
     */
    List<ActionModel> getNodeActions();

    /**
     * Get the action associated to a node.
     *
     * @param n the node
     * @return the associated action, may be null
     */
    ActionModel getNodeAction(UUID n);

    /**
     * Get the evaluator to estimate the duration of the actions.
     *
     * @return a DurationEvaluator
     */
    DurationEvaluator getDurationEvaluator();

    /**
     * Get  all the demanding slices in the model.
     *
     * @return a list of slice. May be empty
     */
    List<Slice> getDSlices();

    /**
     * Get all the consuming slices in the model.
     *
     * @return a list of slice. May be empty
     */
    List<Slice> getCSlices();

    /**
     * Get all the actions associated to a list of virtual machines.
     *
     * @param id the virtual machines
     * @return a list of actions. The order is the same than the order of the VMs.
     */
    List<ActionModel> getVMActions(Set<UUID> id);

    /**
     * Get the moment the virtual machine is ready to be running.
     *
     * @param vm the virtual machine
     * @return {@code null} if the VM is already ready, a variable otherwise
     */
    IntDomainVar getTimeVMReady(UUID vm);

    /**
     * Extract the resulting reconfiguration plan if the
     * solving process succeeded.
     *
     * @return a plan if the solving process succeeded or {@code null}
     */
    ReconfigurationPlan extractSolution();

    /**
     * Get statistics about the solving process
     *
     * @return some statistics
     */
    SolvingStatistics getSolvingStatistics();

    Solver getSolver();
}
