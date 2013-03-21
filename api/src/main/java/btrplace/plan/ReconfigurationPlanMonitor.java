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
     * Commit an action.
     * If it is theoretically possible to execute the action
     * on the current model, the model is updated to reflect the action
     * execution.
     *
     * @param a the action to commit
     * @return a set of unblocked actions that may be empty
     * @throws ReconfigurationPlanMonitorException
     *          if the commit is not possible
     */
    Set<Action> commit(Action a) throws ReconfigurationPlanMonitorException;

    /**
     * Indicates a given feasible action is started
     *
     * @param a the action
     * @return {@code true} iff the action is allowed to start
     */
    boolean begin(Action a);

    /**
     * Indicate whether a reconfiguration is terminated or not.
     * A reconfiguration is terminated if all the actions in the plan
     * have been committed.
     *
     * @return {@code true} iff the reconfiguration is terminated
     */
    boolean isOver();

    boolean isBlocked(Action a);

    ReconfigurationPlan getReconfigurationPlan();
}
