package plan;

import btrplace.instance.Instance;
import plan.actions.Action;
import java.util.Set;

/**
 * A reconfiguration plan is a set of actions to execute
 * to reconfigure an infrastructure starting from a given instance.
 * @author Fabien Hermenier
 */
public interface ReconfigurationPlan extends Iterable<Action> {

    /**
     * Get the instance that is used as a starting point
     * to perform the reconfiguration
     *
     * @return the source configuration
     */
    Instance getSource();

    /**
     * Add a new action to a plan.
     *
     * @param a the action to add
     */
    void add(Action a);

    /**
     * Get the number of action in the plan.
     *
     * @return a positive integer
     */
    int size();

    /**
     * Return the theoretical duration of a reconfiguration plan.
     *
     * @return the finish moment of the last action to execute
     */
    int getDuration();

    /**
     * Get all the actions to perform.
     *
     * @return a list of actions. May be empty
     */
    Set<Action> getActions();
}
