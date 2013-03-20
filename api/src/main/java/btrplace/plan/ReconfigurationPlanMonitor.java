package btrplace.plan;

import btrplace.model.Model;

import java.util.Set;

/**
 * This allows to monitor the execution of a reconfiguration plan while
 * considering the dependencies between the actions that is established
 * in a {@link ReconfigurationPlan}.
 *
 * With regards to the actions that have already been executed, it
 * is possible to get the actions that can be safely executed.
 *
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationPlanMonitor {

    /**
     * Get all the feasible actions that
     * are not currently pending.
     *
     * @return a set of actions that may be empty.
     */
    Set<Action> getFeasibleActions();

    /**
     * Get the actions that cannot be executed for the
     * moment due to un-met dependencies.
     *
     * @return a set of actions that may be empty.
     */
    Set<Action> getBlockedActions();

    /**
     * Get the actions that have began but that
     * are not committed.
     * @return a set of actions that may be empty.
     */
    Set<Action> getPendingActions();

    /**
     * Reset the executor.
     */
    void reset();

    /**
     * Get the current model.
     *
     * @return a model
     */
    Model getCurrentModel();

    /**
     * Commit an action.
     * If it is theoretically possible to execute the action
     * on the current model, the model is updated to reflect the action
     * execution.
     *
     * @param a the action to commit
     * @return {@code true} iff the action was executed on the current model.
     */
    boolean commit(Action a);

    /**
     * Indicates a given feasible action is started
     * @param a the action
     * @return {@code true} iff the acion is allowed to start
     */
    boolean begin(Action a);

    /**
     * Indicate whether a reconfiguration is terminated or not.
     * A reconfiguration is terminated if all the actions in the plan
     * have been committed. This means {@link #getFeasibleActions()} and {@link #getBlockedActions()}
     * are empty.
     *
     * @return {@code true} iff the reconfiguration is terminated
     */
    boolean isOver();
}
