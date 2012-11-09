package btrplace.plan.actions;

import btrplace.instance.Instance;

/**
 * An action to perform on an element and that will alter an instance on success.
 *
 * @author Fabien Hermenier
 */
public interface Action {

    /**
     * Apply the action on an instance.
     *
     * @param i the instance to alter with the action
     * @return {@code true} if the action was applied successfully
     */
    boolean apply(Instance i);

    /**
     * Get the moment the action starts.
     *
     * @return a positive integer
     */
    int getStart();

    /**
     * Get the moment the action ends.
     *
     * @return a positive integer
     */
    int getEnd();
}
