package btrplace.plan;

import btrplace.model.Model;

/**
 * Basic interface for a reconfiguration algorithm.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationAlgorithm {

    /**
     * Compute a reconfiguration plan to reach a solution to the model
     *
     * @param i the current model
     * @return the plan to execute to reach the new solution
     */
    ReconfigurationPlan solve(Model i);
}
