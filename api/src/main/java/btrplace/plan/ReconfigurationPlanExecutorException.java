package btrplace.plan;

import btrplace.model.Model;

/**
 * An exception related to a reconfiguration failure.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanExecutorException extends Exception {

    private ReconfigurationPlan plan;

    private Action failed;

    private Model currentModel;

    /**
     * Make a new exception.
     *
     * @param p            the reconfiguration plan
     * @param failed       the action that has failed
     * @param currentModel the theoretical model at the moment the action failed
     * @param message      the error message
     */
    public ReconfigurationPlanExecutorException(ReconfigurationPlan p, Model currentModel, Action failed, String message) {
        super(message);
        this.plan = p;
        this.currentModel = currentModel;
        this.failed = failed;
    }

    /**
     * Get the reconfiguration plan.
     *
     * @return the plan
     */
    public ReconfigurationPlan getReconfigurationPlan() {
        return plan;
    }

    /**
     * Get the action that failed
     *
     * @return the action
     */
    public Action getFailedAction() {
        return failed;
    }

    /**
     * Get the theoretical model at the moment the action failed.
     *
     * @return the model
     */
    public Model getCurrentModel() {
        return currentModel;
    }

}
