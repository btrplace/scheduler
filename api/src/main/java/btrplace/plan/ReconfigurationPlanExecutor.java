package btrplace.plan;

import btrplace.model.Model;

import java.util.Set;

/**
 * This allows to execute a reconfiguration plan while
 * considering the dependencies between actions rather only their start moment
 * and duration. Each time an action is committed, it update the running model
 * to reflect the changes.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationPlanExecutor {

    /**
     * Get all the feasible actions that
     * are not currently pending.
     *
     * @return a set of actions that may be empty.
     */
    Set<Action> getFeasibleActions();

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
     * Get the actions that cannot be executed for the
     * moment due to un-met dependencies.
     *
     * @return a set of actions that may be empty.
     */
    Set<Action> getBlockedActions();

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
