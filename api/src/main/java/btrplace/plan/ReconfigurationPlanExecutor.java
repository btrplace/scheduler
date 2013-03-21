package btrplace.plan;

import btrplace.plan.event.ActionVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Execute a reconfiguration plan with the help of
 * a {@link ReconfigurationPlanMonitor}.
 * <p/>
 * Each time an action is feasible, the executor starts to execute
 * it in parallel. Once the  execution is terminated, the
 * action is committed and the new feasible actions are executed.
 * This is repeated until the reconfiguration plan has been completely applied.
 * <p/>
 * The practical execution of each of the action is performed by the implementation of an {@link ActionVisitor}
 * where is {@code visit} method should return a non-null value to indicate the success of the operation.
 * <p/>
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanExecutor {

    private ReconfigurationPlanMonitor rpe;

    private ActionVisitor executor;

    private final Logger logger = LoggerFactory.getLogger("PlanExecutor");

    private final Object locker;

    /**
     * Make a new executor.
     *
     * @param rpe      the monitor to rely on.
     * @param executor the object that will execute an action in practice.
     */
    public ReconfigurationPlanExecutor(ReconfigurationPlanMonitor rpe, ActionVisitor executor) {
        this.rpe = rpe;
        this.executor = executor;
        locker = new Object();
    }

    /**
     * Start the reconfiguration.
     */
    public void run() throws InterruptedException, ReconfigurationPlanMonitorException {

        Set<Action> feasible = new HashSet<Action>();
        for (Action a : rpe.getReconfigurationPlan()) {
            if (!rpe.isBlocked(a)) {
                feasible.add(a);
            }
        }
        if (!feasible.isEmpty()) {
            for (Action a : feasible) {
                executeInParallel(a);
            }
            synchronized (locker) {
                locker.wait();
            }
        }
    }

    private void commitAndContinue(Action a) throws ReconfigurationPlanMonitorException {
        logger.debug("action committed: {}", a);
        Set<Action> unblocked = rpe.commit(a);
        if (unblocked.isEmpty()) {
            if (rpe.isOver()) {
                synchronized (locker) {
                    locker.notify();
                }
            }
        } else {
            for (final Action a2 : unblocked) {
                executeInParallel(a2);
            }
        }
    }

    private void executeInParallel(final Action a) throws ReconfigurationPlanMonitorException {
        logger.debug("Start action :{}", a);
        rpe.begin(a);
        Thread t = new Thread() {
            public void run() {
                if (a.visit(executor) != null) {
                    try {
                        commitAndContinue(a);
                    } catch (ReconfigurationPlanMonitorException ex) {
                        System.err.println(ex.getMessage() + "\n" + ex.getAction() + "\n" + ex.getModel().getMapping());
                    }
                } else {
                    //We stop the execution here
                    logger.error("No more feasible actions but the execution is not over");
                    synchronized (locker) {
                        locker.notify();
                    }
                }
            }
        };
        t.start();
    }
}
