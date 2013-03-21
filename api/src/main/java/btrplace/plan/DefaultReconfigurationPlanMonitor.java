package btrplace.plan;

import btrplace.model.Model;

import java.util.*;

/**
 * Simulated execution of a {@link ReconfigurationPlan}.
 * The execution relies on the dependencies between the actions, retrieved using
 * {@link ReconfigurationPlan#getDirectDependencies(Action)}.
 * <p/>
 * The dependencies are updated each time an action is committed, which means the action
 * have been successfully executed.
 * <p/>
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlanMonitor implements ReconfigurationPlanMonitor {

    private ReconfigurationPlan plan;

    private Model curModel;

    private final Map<Action, Set<Dependency>> pre;

    private final Map<Action, Dependency> deps;

    private final Set<Action> pendingActions;

    private final Object lock;

    private int nbCommitted;

    /**
     * Make a new executor.
     *
     * @param plan the plan to execute
     */
    public DefaultReconfigurationPlanMonitor(ReconfigurationPlan plan) {
        this.plan = plan;

        pendingActions = new HashSet<Action>();
        pre = new HashMap<Action, Set<Dependency>>();
        deps = new HashMap<Action, Dependency>();
        lock = new Object();
        reset();
    }

    private void reset() {
        synchronized (lock) {
            curModel = plan.getOrigin().clone();
            pre.clear();
            pendingActions.clear();
            nbCommitted = 0;
            for (Action a : plan) {
                Set<Action> dependencies = plan.getDirectDependencies(a);
                if (dependencies.isEmpty()) {
                    deps.put(a, new Dependency(a, Collections.<Action>emptySet()));
                } else {
                    Dependency dep = new Dependency(a, dependencies);
                    deps.put(a, dep);
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
    }

    @Override
    public Model getCurrentModel() {
        return curModel;
    }

    @Override
    public Set<Action> commit(Action a) throws ReconfigurationPlanMonitorException {
        Set<Action> s = new HashSet<Action>();
        synchronized (lock) {
            if (!pendingActions.remove(a)) {
                throw new ReconfigurationPlanMonitorException(curModel, a, "The action was not supposed to be executed");
            }
            boolean ret = a.apply(curModel);
            if (!ret) {
                throw new ReconfigurationPlanMonitorException(curModel, a, "unable to apply the action of the model");
            }
            nbCommitted++;
            //Browse all its dependencies for the action
            Set<Dependency> deps = pre.get(a);
            if (deps != null) {
                for (Dependency dep : deps) {
                    Set<Action> actions = dep.getDependencies();
                    actions.remove(a);
                    if (actions.isEmpty()) {
                        Action x = dep.getAction();
                        s.add(x);
                    }
                }
            }
        }
        return s;
    }

    @Override
    public boolean begin(Action a) {
        synchronized (lock) {
            Dependency dep = deps.get(a);
            return dep.getDependencies().isEmpty() && pendingActions.add(a);
        }
    }

    @Override
    public boolean isOver() {
        synchronized (lock) {
            return plan.getSize() == nbCommitted;
        }
    }

    @Override
    public boolean isBlocked(Action a) {
        synchronized (lock) {
            return !deps.get(a).getDependencies().isEmpty();
        }
    }

    @Override
    public ReconfigurationPlan getReconfigurationPlan() {
        return plan;
    }
}
