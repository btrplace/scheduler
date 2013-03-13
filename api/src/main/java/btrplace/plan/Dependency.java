package btrplace.plan;

import java.util.HashSet;
import java.util.Set;

/**
 * Store the dependencies for an action. A dependency is another action
 * that must be executed in an earlier stage to make the original action feasible.
 *
 * @author Fabien Hermenier
 */
public class Dependency {

    private Action a;

    private Set<Action> deps;

    /**
     * Make a new dependency.
     *
     * @param action       the action
     * @param dependencies its dependencies.
     */
    public Dependency(Action action, Set<Action> dependencies) {
        a = action;
        deps = new HashSet<Action>(dependencies);
    }

    /**
     * Get the action.
     *
     * @return an action
     */
    public Action getAction() {
        return a;
    }

    /**
     * Get the actions {@link #getAction()} depends on.
     *
     * @return a set of actions that may be empty.
     */
    public Set<Action> getDependencies() {
        return deps;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(deps).append(" -> ").append(a);
        return b.toString();
    }
}
