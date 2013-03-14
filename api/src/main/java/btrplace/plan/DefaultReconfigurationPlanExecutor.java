package btrplace.plan;

import btrplace.model.Model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Simulated execution of a {@link ReconfigurationPlan}.
 * The execution relies on the dependencies between the actions, retrieved using
 * {@link ReconfigurationPlan#getDirectDependencies(Action)}.
 * <p/>
 * The dependencies are updated each time an action is committed, which means the action
 * have been successfully executed.
 * <p/>
 * TODO: must be thread-safe
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlanExecutor implements ReconfigurationPlanExecutor {

    private ReconfigurationPlan plan;

    private Model curModel;

    private Map<Action, Set<Dependency>> pre;

    private Set<Action> feasibleActions;

    private Set<Action> waitingActions;

    /**
     * Make a new executor.
     *
     * @param plan the plan to execute
     */
    public DefaultReconfigurationPlanExecutor(ReconfigurationPlan plan) {
        this.plan = plan;
        reset();
    }

    @Override
    public Set<Action> getFeasibleActions() {
        return feasibleActions;
    }

    @Override
    public void reset() {
        curModel = plan.getOrigin().clone();
        pre = new HashMap<Action, Set<Dependency>>();
        feasibleActions = new HashSet<Action>();
        waitingActions = new HashSet<Action>();
        for (Action a : plan) {
            Set<Action> dependencies = plan.getDirectDependencies(a);
            if (dependencies.isEmpty()) {
                feasibleActions.add(a);
            } else {
                waitingActions.add(a);
                Dependency dep = new Dependency(a, dependencies);
                for (Action x : dep.getDependencies()) {
                    Set<Dependency> pres = pre.get(x);
                    if (pres == null) {
                        pres = new HashSet<Dependency>();
                        pre.put(x, pres);
                    }
                    pres.add(dep);
                }
            }
        }

    }

    @Override
    public Model getCurrentModel() {
        return curModel;
    }

    @Override
    public boolean commit(Action a) {
        if (!feasibleActions.remove(a)) {
            return false;
        }
        boolean ret = a.apply(curModel);
        if (!ret) {
            return false;
        }
        //Browse all its dependencies for the action
        Set<Dependency> deps = pre.get(a);
        if (deps != null) {
            for (Dependency dep : deps) {
                Set<Action> actions = dep.getDependencies();
                actions.remove(a);
                if (actions.isEmpty()) {
                    feasibleActions.add(dep.getAction());
                    waitingActions.remove(dep.getAction());
                }
            }
        }
        assert !deadlock();
        return true;
    }

    /**
     * Check if there is a deadlock
     *
     * @return {@code true} iff there is a deadlock
     */
    private boolean deadlock() {
        return feasibleActions.isEmpty() && !waitingActions.isEmpty();
    }

    @Override
    public Set<Action> getWaitingActions() {
        return waitingActions;
    }

    @Override
    public boolean isOver() {
        return getWaitingActions().isEmpty() && getFeasibleActions().isEmpty();
    }
}
