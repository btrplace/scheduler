package btrplace.plan;

import btrplace.plan.event.ActionVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
 * <p/>
 * TODO: Error reporting
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanExecutor {

    private ReconfigurationPlanMonitor rpe;

    private ActionVisitor executor;

    private final Logger logger = LoggerFactory.getLogger("PlanExecutor");

    private Lock lock;

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
        if (!feasible.isEmpty()) {
            lock = new ReentrantLock();
            lock.lock();
            for (final Action a : feasible) {
                executeInParallel(a);
            }
            lock.lock();
        }
    }

    private void commitAndContinue(Action a) {
        rpe.commit(a);
        Set<Action> feasible = rpe.getFeasibleActions();
        if (feasible.isEmpty()) {
            if (!rpe.isOver()) {
                logger.error("No more feasible actions but the execution is not over:\npendings={}\nblocked={}\n", rpe.getPendingActions(), rpe.getBlockedActions());
            }
            lock.unlock();
        } else {
            for (final Action a2 : rpe.getFeasibleActions()) {
                executeInParallel(a2);
            }
        }
    }

    private void executeInParallel(final Action a) {
        Thread t = new Thread() {
            public void run() {
                if (a.visit(executor) != null) {
                    commitAndContinue(a);
                } else {
                    //We stop the execution here
                    logger.error("No more feasible actions but the execution is not over:\npendings={}\nblocked={}\n", rpe.getPendingActions(), rpe.getBlockedActions());
                    lock.unlock();
                }
            }
        };
        t.start();
    }
}
