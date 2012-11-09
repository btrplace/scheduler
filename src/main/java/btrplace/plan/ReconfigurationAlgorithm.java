package btrplace.plan;

import btrplace.instance.Instance;
import btrplace.instance.constraint.SatConstraint;

import java.util.Collection;

/**
 * Basic interface for a reconfiguration algorithm.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationAlgorithm {

    /**
     * Compute a reconfiguration plan to reach a new instance
     * that satisfy all the constraints.
     *
     * @param i           the current instance
     * @param constraints the constraints to satisfy
     * @return the plan to execute to reach the new solution
     */
    ReconfigurationAlgorithm compute(Instance i, Collection<SatConstraint> constraints);
}
