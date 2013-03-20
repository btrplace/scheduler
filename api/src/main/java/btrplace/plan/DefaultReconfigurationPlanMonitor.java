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
 * TODO: must be thread-safe
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlanMonitor implements ReconfigurationPlanMonitor {

    private ReconfigurationPlan plan;

    private Model curModel;

    private Map<Action, Set<Dependency>> pre;

    private final Set<Action> feasibleActions;

    private final Set<Action> pendingActions;

    private final Set<Action> blockedActions;

    /**
     * Make a new executor.
     *
     * @param plan the plan to execute
     */
    public DefaultReconfigurationPlanMonitor(ReconfigurationPlan plan) {
        this.plan = plan;

        feasibleActions = new HashSet<Action>();
        blockedActions = new HashSet<Action>();
        pendingActions = new HashSet<Action>();

        reset();
    }

    @Override
    public Set<Action> getFeasibleActions() {
        return Collections.unmodifiableSet(feasibleActions);
    }

    @Override
    public Set<Action> getPendingActions() {
        return Collections.unmodifiableSet(pendingActions);
    }

    @Override
    public Set<Action> getBlockedActions() {
        return Collections.unmodifiableSet(blockedActions);
    }


    @Override
    public void reset() {
        synchronized(feasibleActions) {
            synchronized (blockedActions) {
                synchronized (pendingActions) {
                    curModel = plan.getOrigin().clone();
                    pre = new HashMap<Action, Set<Dependency>>();
                    pendingActions.clear();
                }
                feasibleActions.clear();
                blockedActions.clear();

                for (Action a : plan) {
                    Set<Action> dependencies = plan.getDirectDependencies(a);
                    if (dependencies.isEmpty()) {
                        feasibleActions.add(a);
                    } else {
                        blockedActions.add(a);
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
        }
    }

    @Override
    public Model getCurrentModel() {
        return curModel;
    }

    @Override
    public boolean commit(Action a) {
        synchronized (pendingActions) {
            if (!pendingActions.remove(a)) {
                return false;
            }
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
                    synchronized(feasibleActions) {
                        synchronized (blockedActions) {
                            feasibleActions.add(dep.getAction());
                            blockedActions.remove(dep.getAction());
                        }
                    }
                }
            }
        }

        /**
         * Check if there is a deadlock.
         * A deadlock occurs when some actions are blocked while their is no feasible and pending actions.
         */
        assert !deadLock();
        return true;
    }

    private boolean deadLock() {
        synchronized(feasibleActions) {
            synchronized (pendingActions) {
                synchronized (blockedActions) {
                    return feasibleActions.isEmpty() && pendingActions.isEmpty() && !blockedActions.isEmpty();
                }
            }
        }
    }

    @Override
    public boolean begin(Action a) {
        synchronized(feasibleActions) {
            synchronized (pendingActions) {
                return feasibleActions.remove(a) && pendingActions.add(a);
            }
        }
    }

    @Override
    public boolean isOver() {
        synchronized(feasibleActions) {
            synchronized (pendingActions) {
                synchronized (blockedActions) {
                    return getBlockedActions().isEmpty()
                            && getFeasibleActions().isEmpty()
                            && getPendingActions().isEmpty();
                }
            }
        }
    }
}
