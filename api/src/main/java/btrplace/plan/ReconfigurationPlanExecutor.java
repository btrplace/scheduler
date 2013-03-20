package btrplace.plan;

import btrplace.plan.event.ActionVisitor;

import java.util.Set;

/**
 * Execute a reconfiguration plan with the help of
 * a {@link ReconfigurationPlanMonitor}.
 * <p/>
 * Each time an action is feasible, the executor starts to execute
 * the action in parallel. Once the action execution is terminated, the
 * action is committed and the new feasible actions are executed.
 * This is repeated until the reconfiguration plan has been completely applied.
 * <p/>
 * The practical execution of each of the action is performed by the implementation of an {@link ActionVisitor}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanExecutor {

    private ReconfigurationPlanMonitor rpe;

    private ActionVisitor executor;

    /**
     * Make a new executor.
     *
     * @param rpe      the monitor to rely on.
     * @param executor the object that will execute an action in practice.
     */
    public ReconfigurationPlanExecutor(ReconfigurationPlanMonitor rpe, ActionVisitor executor) {
        this.rpe = rpe;
        this.executor = executor;
    }

    /**
     * Start the reconfiguration.
     */
    public void run() {
        Set<Action> feasible = rpe.getFeasibleActions();
        for (final Action a : feasible) {
            executeInParallel(a);
        }
    }

    private void commitAndContinue(Action a) {
        rpe.commit(a);
        for (final Action a2 : rpe.getFeasibleActions()) {
            executeInParallel(a2);
        }
    }

    private void executeInParallel(final Action a) {
        Thread t = new Thread() {
            public void run() {
                a.visit(executor);
                commitAndContinue(a);
            }
        };
        t.start();
    }
}
