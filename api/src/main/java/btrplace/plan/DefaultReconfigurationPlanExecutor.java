package btrplace.plan;

import btrplace.model.Model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Must be thread-safe
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlanExecutor implements ReconfigurationPlanExecutor {

    private ReconfigurationPlan plan;

    private Model curModel;

    private Map<Action, Set<Dependency>> pre;

    private Set<Action> feasibleActions;

    public DefaultReconfigurationPlanExecutor(ReconfigurationPlan plan) {
        this.plan = plan;
        curModel = plan.getOrigin().clone();
        pre = new HashMap<Action, Set<Dependency>>();
        feasibleActions = new HashSet<Action>();

        for (Action a : plan) {
            Set<Action> dependencies = plan.getDirectDependencies(a);
            if (dependencies.isEmpty()) {
                feasibleActions.add(a);
            } else {
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
    public Set<Action> getFeasibleActions() {
        return feasibleActions;
    }

    @Override
    public void reset() {
        curModel = plan.getOrigin().clone();
    }

    @Override
    public Model getCurrentModel() {
        return curModel;
    }

    @Override
    public boolean commit(Action a) {
        boolean ret = a.apply(curModel);
        if (!ret) {
            return false;
        }
        //Browse all the dependencies for the action, and update them
        Set<Dependency> deps = pre.get(a);
        for (Dependency dep : deps) {
            Set<Action> actions = dep.getDependencies();
            actions.remove(a);
            if (actions.isEmpty()) {
                feasibleActions.add(a);
            }
        }
        return true;
    }

    @Override
    public Set<Action> getWaitingActions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOver() {
        throw new UnsupportedOperationException();
    }
}
