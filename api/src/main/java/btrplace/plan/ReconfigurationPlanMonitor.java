package btrplace.plan;

import btrplace.model.Model;

import java.util.Set;

/**
 * This allows to monitor the execution of a reconfiguration plan while
 * considering the dependencies between the actions that is established
 * in a {@link ReconfigurationPlan}.
 * <p/>
 * With regards to the actions that have already been executed, it
 * is possible to get the actions that can be safely executed.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationPlanMonitor {

    /**
     * Get the current model.
     *
     * @return a model
     */
    Model getCurrentModel();

    /**
     * Commit an action that must not be blocked.
     * If it is theoretically possible to execute the action on the current model,
     * the model is updated accordingly.
     *
     * @param a the action to commit
     * @return a set of unblocked actions that may be empty if the operation succeed.
     *         {@code null} if the commit was not allowed because the action was not applyable
     */
    Set<Action> commit(Action a);

    /**
     * Get the number of actions that have been committed.
     *
     * @return a number between 0 and {@link btrplace.plan.ReconfigurationPlan#getSize()}
     */
    int getNbCommitted();

    boolean isBlocked(Action a);

    ReconfigurationPlan getReconfigurationPlan();
}
