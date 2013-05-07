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
import btrplace.solver.choco.actionModel.NodeActionModel;
import btrplace.solver.choco.actionModel.VMActionModel;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import btrplace.solver.choco.objective.ObjectiveAlterer;
import btrplace.solver.choco.view.ChocoModelView;
import btrplace.solver.choco.view.ModelViewMapper;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.slf4j.Logger;

import java.util.Collection;
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
    VMActionModel[] getVMActions();

    /**
     * Get the action associated to a given VM.
     *
     * @param id the VM identifier
     * @return the associated action if exists, {@code null} otherwise
     */
    VMActionModel getVMAction(UUID id);

    /**
     * Get all the actions associated to a list of virtual machines.
     *
     * @param id the virtual machines
     * @return a list of actions. The order is the same than the order of the VMs.
     */
    VMActionModel[] getVMActions(Set<UUID> id);


    /**
     * Get all the actions related to nodes.
     *
     * @return a list of actions.
     */
    NodeActionModel[] getNodeActions();

    /**
     * Get the action associated to a given node.
     *
     * @param id the node identifier
     * @return the associated action if exists, {@code null} otherwise
     */
    NodeActionModel getNodeAction(UUID id);

    /**
     * Get the evaluator to estimate the duration of the actions.
     */
    DurationEvaluators getDurationEvaluators();

    /**
     * Solve the RP and return a solution if exists.
     *
     * @param timelimit the timeout in second. Must be superior to 0 to be considered
     * @param optimize  {@code true} to make the solver try to improve the first computed solution.
     * @return a plan if the solving process succeeded, {@code null} if the solver was not able to compute
     *         a solution.
     * @throws SolverException if an error occurs
     */
    ReconfigurationPlan solve(int timelimit, boolean optimize) throws SolverException;

    /**
     * Get the CPSolver used to model this problem.
     *
     * @return the CPSolver
     */
    CPSolver getSolver();

    /**
     * Create a variable that indicate the placement of an element on a node.
     *
     * @param n the variable label as a possible concatenation of objects
     * @return a variable
     */
    IntDomainVar makeHostVariable(Object... n);

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
     * @param n the variable label. The toString() representation of the objects will be used
     * @return the created variable.
     */
    IntDomainVar makeUnboundedDuration(Object... n);

    /**
     * Create a variable that indicate a moment.
     *
     * @param ub the variable upper bound
     * @param lb the variable lower bound
     * @param n  the variable label. The toString() representation of the objects will be used
     * @return the created variable with a upper-bound necessarily lesser than {@code getEnd().getSup()}
     * @throws SolverException if the bounds are not valid
     */
    IntDomainVar makeDuration(int ub, int lb, Object... n) throws SolverException;

    /**
     * Get the view associated to a given identifier.
     *
     * @param id the view identifier
     * @return the view if exists, {@code null} otherwise
     */
    ChocoModelView getView(String id);

    /**
     * Get the view mapper that is used to associate
     * {@link btrplace.model.view.ModelView} to {@link ChocoModelView}.
     *
     * @return the mapper
     */
    ModelViewMapper getViewMapper();

    /**
     * Get all the declared views.
     *
     * @return a collection of views, may be empty
     */
    Collection<ChocoModelView> getViews();

    /**
     * Get the amount of VMs hosted on each node.
     *
     * @return an array of variable counting the number of VMs on each node
     */
    IntDomainVar[] getNbRunningVMs();


    /**
     * Make a label for a variable iff the labelling is enabled
     *
     * @param lbl the label to make
     * @return the label that will be used in practice
     */
    String makeVarLabel(Object... lbl);

    /**
     * Check if variables labelling is enabled.
     *
     * @return {@code true} iff enabled
     */
    boolean isVarLabelling();

    /**
     * Get the VMs managed by the solver.
     * This set contains all the VMs that will have their state changed
     * plus the set of manageable running VMs that was passed to the constructor.
     *
     * @return a set of VMs identifier
     */
    Set<UUID> getManageableVMs();

    /**
     * Get the builder that handle the scheduling part of the problem.
     *
     * @return the builder
     */
    SliceSchedulerBuilder getTaskSchedulerBuilder();

    /**
     * Get the builder that handle the management of capacities over the time.
     *
     * @return the builder
     */
    AliasedCumulativesBuilder getAliasedCumulativesBuilder();

    /**
     * Get the builder that handle the VM placement at the end of the reconfiguration process.
     *
     * @return the builder
     */
    BinPackingBuilder getBinPackingBuilder();

    /**
     * Get the logger.
     *
     * @return well, the logger
     */
    Logger getLogger();

    /**
     * Get the alterer that is used to manipulate the objective value
     * each time a solution is computed
     *
     * @return the alterer if it was defined, {@code null} otherwise
     */
    ObjectiveAlterer getObjectiveAlterer();

    /**
     * Set the alterer to use for this problem
     *
     * @param a the alterer to use
     */
    void setObjectiveAlterer(ObjectiveAlterer a);

    /**
     * Get the pool that is used to book UUIDs.
     *
     * @return the pool
     */
    UUIDPool getUUIDPool();

    /**
     * Create a clone of a given VM.
     * The clone will take the place of the VM by the end of the reconfiguration process.
     *
     * @param vm the current VM to substitute
     * @return the identifier of the new VM. {@code null} if the process failed
     */
    UUID cloneVM(UUID vm);
}
