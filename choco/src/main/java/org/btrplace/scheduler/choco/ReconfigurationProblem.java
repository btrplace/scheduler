/*
 * Copyright  2022 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco;

import org.btrplace.model.*;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.SchedulerModelingException;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.btrplace.scheduler.choco.transition.NodeTransition;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.btrplace.scheduler.choco.view.ChocoView;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * Interface for the Reconfiguration Problem.
 * <p>
 * VM and node identifiers are translated to a position in an array.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationProblem {

    /**
     * Get the current location of a running or a sleeping VM.
     *
     * @param vmIdx the index of the VM
     * @return the node index if exists, -1 if the VM is unknown
     */
    int getCurrentVMLocation(int vmIdx);

    /**
     * Get all the nodes in the model. Indexed by their identifier.
     *
     * @return an immutable list of Nodes.
     */
    List<Node> getNodes();

    /**
     * Get all the VMs in the model. Indexed by their identifier.
     *
     * @return an immutable list of VMs
     */
    List<VM> getVMs();

    /**
     * Get the initial Model.
     *
     * @return a model
     */
    Model getSourceModel();

    /**
     * Get the VMs that will be in the running state at the
     * end of the reconfiguration process.
     *
     * @return a set, may be empty
     */
    Set<VM> getFutureRunningVMs();

    /**
     * Get the VMs that will be in the ready state at the
     * end of the reconfiguration process.
     *
     * @return a set, may be empty
     */
    Set<VM> getFutureReadyVMs();

    /**
     * Get the VMs that will be in the sleeping state at the
     * end of the reconfiguration process.
     *
     * @return a set, may be empty
     */
    Set<VM> getFutureSleepingVMs();

    /**
     * Get the VMs that will be killed at the
     * end of the reconfiguration process.
     *
     * @return a set, may be empty
     */
    Set<VM> getFutureKilledVMs();

    /**
     * Get the next state for a given VM.
     *
     * @param v the VM
     * @return the state if the VM is known, {@code null} otherwise
     */
    VMState getFutureState(VM v);

    /**
     * Get the current VM state.
     * @param v the VM
     * @return its states if the VM is known. {@code null} otherwise
     */
    VMState getSourceState(VM v);

    /**
     * Get the current node state.
     * @param n the node
     * @return its states if the node is known. {@code null} otherwise
     */
    NodeState getSourceState(Node n);

    /**
     * Get the starting moment of the reconfiguration.
     *
     * @return a variable equals to 0
     */
    IntVar getStart();

    /**
     * Get the end  moment of the reconfiguration
     *
     * @return a variable, should be equals to the last end moment of actions
     */
    IntVar getEnd();

    /**
     * Get the index of a VM
     *
     * @param vm the VM
     * @return its index or -1 in case of failure
     */
    int getVM(VM vm);

    /**
     * Get the VM with a specified index.
     *
     * @param idx the index of the VM
     * @return the VM or null in case of failure
     */
    VM getVM(int idx);

    /**
     * Get the index of a node.
     *
     * @param n the node
     * @return its index or -1 in case of failure
     */
    int getNode(Node n);

    /**
     * Get the node with a specified index.
     *
     * @param idx the index of the node
     * @return the node or null in case of failure
     */
    Node getNode(int idx);

    /**
     * Get the VMs transition.
     *
     * @return an immutable list of transitions.
     */
    List<VMTransition> getVMActions();

    /**
     * Get the transition associated to a given VM.
     *
     * @param id the VM identifier
     * @return the associated transition if exists, {@code null} otherwise
     */
    VMTransition getVMAction(VM id);

    /**
     * Get the transitions associated to a set of VMs.
     *
     * @param id the VMs
     * @return an immutable list of transitions.
     */
    List<VMTransition> getVMActions(Collection<VM> id);


    /**
     * Get all the nodes transition.
     * @return an immutable list of transitions.
     */
    List<NodeTransition> getNodeActions();

    /**
     * Get the transition associated to a given node.
     *
     * @param id the node identifier
     * @return the associated transition if exists, {@code null} otherwise
     */
    NodeTransition getNodeAction(Node id);

    /**
     * Get the evaluator to estimate the duration of the actions.
     * @return the current evaluator.
     */
    DurationEvaluators getDurationEvaluators();

    /**
     * Solve the RP and return a solution if exists.
     *
     * @param timeLimit the timeout in second. Must be superior to 0 to be considered
     * @param optimize  {@code true} to make the solver try to improve the first computed solution.
     * @return a plan if the solving process succeeded, {@code null} if the solver was not able to compute
     * a solution.
     * @throws SchedulerException if an error occurred
     */
    ReconfigurationPlan solve(int timeLimit, boolean optimize) throws SchedulerException;

    /**
     * Build a plan for a solution.
     * @param s the solution
     * @param src the source model
     * @return the resulting plan
     * @throws SchedulerException if a error occurred
     */
    ReconfigurationPlan buildReconfigurationPlan(Solution s, Model src) throws SchedulerException;

    /**
     * Return all the solutions that have been computed from a previous {@link #solve(int, boolean)} call.
     * @return a list of plan that may be empty
     * @throws SchedulerException if an error occurred
     */
    List<ReconfigurationPlan> getComputedSolutions() throws SchedulerException;

    /**
     * Get the Solver used to model this problem.
     *
     * @return the Solver
     */
    Solver getSolver();

    /**
     * Get the CSP model to solve
     *
     * @return the model
     */
    org.chocosolver.solver.Model getModel();

    /**
     * Create a variable that indicate the placement of an element on a node.
     *
     * @param n the variable label as a possible concatenation of objects
     * @return a variable
     */
    IntVar makeHostVariable(Object... n);

    /**
     * Create a variable that indicate the placement of an element on a node.
     * @return a variable
     */
    IntVar makeHostVariable();

    /**
     * Create a variable that indicate the placement of an element on a node.
     *
     * @param candidates the candidate nodes.
     * @param n          the variable label as a possible concatenation of objects
     * @return a variable
     */
    IntVar makeHostVariable(List<Node> candidates, Object... n);

    /**
     * Create a variable that indicate the current placement of a VM.
     * The variable is then already instantiated
     *
     * @param vmId the VM identifier
     * @param n    the variable label
     * @return the created variable
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred while creating the variable
     */
    IntVar makeCurrentHost(VM vmId, Object... n) throws SchedulerException;

    /**
     * Create a variable that indicate the current placement of a VM.
     * The variable is then already instantiated
     *
     * @param vmId the VM identifier
     * @return the created variable
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred while creating the variable
     */
    IntVar makeCurrentHost(VM vmId) throws SchedulerException;

    /**
     * Create a variable that indicate a given node.
     * The variable is then already instantiated
     *
     * @param nId the node identifier
     * @param n   the variable label
     * @return the created variable
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred while creating the variable
     */
    IntVar makeCurrentNode(Node nId, Object... n) throws SchedulerException;

    /**
     * Create a variable that indicate a given node.
     * The variable is then already instantiated
     *
     * @param nId the node identifier
     * @return the created variable
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred while creating the variable
     */
    IntVar makeCurrentNode(Node nId) throws SchedulerException;

    /**
     * Create a variable denoting a duration.
     *
     * @param n the variable label. The toString() representation of the objects will be used
     * @return the created variable.
     */
    IntVar makeUnboundedDuration(Object... n);

    /**
     * Create a variable denoting a duration.
     *
     * @return the created variable.
     */
    IntVar makeUnboundedDuration();

    /**
     * Create a variable that indicate a moment.
     *
     * @param ub the variable upper bound
     * @param lb the variable lower bound
     * @param n  the variable label. The toString() representation of the objects will be used
     * @return the created variable with a upper-bound necessarily lesser than {@code getEnd().getUB()}
     * @throws org.btrplace.scheduler.SchedulerException if the bounds are not valid
     */
    IntVar makeDuration(int ub, int lb, Object... n) throws SchedulerException;

    /**
     * Create a variable that indicates a moment.
     *
     * @param ub the variable upper bound
     * @param lb the variable lower bound
     * @return the created variable with a upper-bound necessarily lesser than {@code getEnd().getUB()}
     * @throws org.btrplace.scheduler.SchedulerException if the bounds are not valid
     */
    IntVar makeDuration(int ub, int lb) throws SchedulerException;

    /**
     * Get the view associated to a given identifier.
     *
     * @param id the view identifier
     * @return the view if exists, {@code null} otherwise
     */
    ChocoView getView(String id);

  /**
   * Get the view associated to a given identifier.
   *
   * @param id the view identifier
   * @return the view
   * @throws SchedulerModelingException if the view is missing.
   */
  default ChocoView getRequiredView(String id) throws SchedulerModelingException {
    ChocoView v = getView(id);
    if (v == null) {
      throw SchedulerModelingException.missingView(getSourceModel(), id);
    }
    return v;
  }

    /**
     * Get all the declared view keys.
     *
     * @return a collection of keys, may be empty
     */
    Collection<String> getViews();

    /**
     * Add a view.
     * There must not be a view with a same identifier already in.
     *
     * @param v the view to add
     * @return {@code true} iff the view has been added.
     */
    boolean addView(ChocoView v);


    /**
     * Get the amount of VMs hosted on each node.
     *
     * @return an immutable list of variables counting the number of VMs on each node
     */
    List<IntVar> getNbRunningVMs();


    /**
     * Make a label for a variable iff the labelling is enabled
     *
     * @param lbl the label to make
     * @return the label that will be used in practice
     */
    String makeVarLabel(Object... lbl);

    /**
     * Make a label for a variable iff the labelling is enabled
     *
     * @param lbl the label to make
     * @return the label that will be used in practice
     */
    String makeVarLabel(Object lbl);

    /**
     * Check if variables are expected to be labelled.
     *
     * @return {@code true} iff variables must be labelled.
     */
    boolean labelVariables();

    /**
     * Make a constant.
     * This methods allows to cache constant if variable labelling is disabled.
     *
     * @param v   the constant
     * @param lbl the label to make
     * @return the variable
     */
    IntVar fixed(int v, Object... lbl);

    /**
     * Make a constant.
     * @param v the constant
     * @param lbl the label.
     * @return the variable.
     */
    IntVar fixed(int v, Object lbl);

    /**
     * Get the VMs managed by the solver.
     * This set contains all the VMs that will have their state changed
     * plus the set of manageable running VMs that was passed to the constructor.
     *
     * @return a set of VMs identifier
     */
    Set<VM> getManageableVMs();

    /**
     * Get the logger.
     *
     * @return well, the logger
     */
    Logger getLogger();

    /**
     * Create a clone of a given VM.
     * The clone will take the place of the VM by the end of the reconfiguration process.
     *
     * @param vm the identifier of the  VM to substitute
     * @return the identifier of the new VM. {@code null} if the process failed
     */
    VM cloneVM(VM vm);

    /**
     * Set the optimisation variable.
     *
     * @param b {@code true} to minimise the value. {@code false} to maximise it
     * @param v the variable to optimise
     */
    void setObjective(boolean b, IntVar v);

    /**
     * Get the optimisation variable
     *
     * @return a variable that is {@code null} if {@code #getResolutionPolicy() == ResolutionPolicy#SATISFACTION}
     */
    IntVar getObjective();

    /**
     * Get the current resolution policy.
     *
     * @return the current resolution policy
     */
    ResolutionPolicy getResolutionPolicy();

    /**
     * Stop the solver.
     */
    void stop();
}
