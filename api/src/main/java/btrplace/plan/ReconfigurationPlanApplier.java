package btrplace.plan;

import btrplace.model.Model;

/**
 * An object to simulate the application of
 * a plan. The result will be a new model.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationPlanApplier {

    /**
     * Apply a plan.
     *
     * @param p the plan to apply
     * @return the resulting model if the application succeed. {@code null} otherwise
     */
    Model apply(ReconfigurationPlan p);

    /**
     * Textual representation of a plan.
     *
     * @param p the plan to stringify
     * @return the formatted string
     */
    String toString(ReconfigurationPlan p);
}
