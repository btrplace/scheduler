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
import btrplace.solver.SolverException;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Interface for the Reconfiguration Problem.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationProblem {

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
    UUID[] getVMs();

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
    Set<UUID> getFutureRunningVMs();

    /**
     * Get the virtual machines that will be in the ready state at the
     * end of the reconfiguration process.
     *
     * @return a set, may be empty
     */
    Set<UUID> getFutureReadyVMs();

    /**
     * Get the virtual machines that will be in the sleeping state at the
     * end of the reconfiguration process.
     *
     * @return a set, may be empty
     */
    Set<UUID> getFutureSleepingVMs();

    /**
     * Get the virtual machines that will be killed at the
     * end of the reconfiguration process.
     *
     * @return a set, may be empty
     */
    Set<UUID> getFutureKilledVMs();

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
    ActionModel[] getVMActions();

    /**
     * Get all the actions associated to a list of virtual machines.
     *
     * @param id the virtual machines
     * @return a list of actions. The order is the same than the order of the VMs.
     */
    ActionModel[] getVMActions(Set<UUID> id);


    /**
     * Get all the actions related to nodes.
     *
     * @return a list of actions.
     */
    ActionModel[] getNodeActions();

    /**
     * Get the evaluator to estimate the duration of the actions.
     */
    DurationEvaluators getDurationEvaluators();

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
     * Extract the resulting reconfiguration plan if the
     * solving process succeeded.
     *
     * @return a plan if the solving process succeeded or {@code null}
     */
    ReconfigurationPlan extractSolution();

    /**
     * Get the CPSolver used to model this problem.
     *
     * @return the CPSolver
     */
    CPSolver getSolver();

    /**
     * Create a variable that indicate the placement of an element on a node.
     *
     * @param n the variable label
     * @return a variable
     */
    IntDomainVar makeHostVariable(String n);

    /**
     * Create a variable that indicate the current placement of a VM.
     * The variable is then already instantiated
     *
     * @param n    the variable label
     * @param vmId the VM identifier
     * @return the created variable
     * @throws SolverException if an error occurred while creating the variable
     */
    IntDomainVar makeCurrentHost(String n, UUID vmId) throws SolverException;

    /**
     * Create a variable that indicate a given node.
     * The variable is then already instantiated
     *
     * @param n   the variable label
     * @param nId the node identifier
     * @return the created variable
     * @throws SolverException if an error occurred while creating the variable
     */
    IntDomainVar makeCurrentNode(String n, UUID nId) throws SolverException;

    /**
     * Create a variable denoting a duration.
     *
     * @param n the variable label
     * @return the created variable.
     */
    IntDomainVar makeDuration(String n);

    /**
     * Create a variable that indicate a moment.
     *
     * @param n  the variable label
     * @param lb the variable lower bound
     * @param ub the variable upper bound
     * @return the created variable with a upper-bound necessarily lesser than {@code getEnd().getSup()}
     * @throws SolverException if the bounds are not valid
     */
    IntDomainVar makeDuration(String n, int lb, int ub) throws SolverException;

    /**
     * Get a resource mapping from its identifier.
     *
     * @param id the resource identifier
     * @return the resource mapping if exists, {@code null} otherwise
     */
    ResourceMapping getResourceMapping(String id);

    /**
     * Get the amount of VMs hosted on each node.
     *
     * @return an array of variable counting the number of VMs on each node
     */
    IntDomainVar[] getVMsCountOnNodes();


    /**
     * Make a label for a variable iff the labelling is enabled
     *
     * @param lbl the label to make
     * @return the label that will be used in practice
     */
    String makeVarLabel(String lbl);

    /**
     * Check if variables labelling is enabled.
     *
     * @return {@code true} iff enabled
     */
    boolean isVarLabelling();

    /**
     * Get the VMs managed by the solver.
     *
     * @return a set of VMs identifier
     */
    Set<UUID> getManageableVMs();
}
