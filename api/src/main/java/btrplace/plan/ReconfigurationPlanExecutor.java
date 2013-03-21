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

    private final int nbActions;

    private ReconfigurationPlanExecutorException ex = null;

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
        nbActions = rpe.getReconfigurationPlan().getSize();
    }

    /**
     * Start the reconfiguration.
     */
    public void run() throws InterruptedException, ReconfigurationPlanExecutorException {

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
            if (ex != null) {
                throw ex;
            }
        }
    }

    public void commit(ActionExecutor ae) {
        if (!ae.succeeded()) {
            logger.error("Action execution failure: " + ae.getAction());
            ex = new ReconfigurationPlanExecutorException(rpe.getReconfigurationPlan(),
                    rpe.getCurrentModel(),
                    ae.getAction(),
                    "The action execution failed");
            synchronized (locker) {
                locker.notify();
            }
        } else {
            logger.debug("Action committed: {}", ae.getAction());
            Set<Action> unblocked = rpe.commit(ae.getAction());
            if (unblocked == null) { //Not applyable action
                ex = new ReconfigurationPlanExecutorException(rpe.getReconfigurationPlan(),
                        rpe.getCurrentModel(),
                        ae.getAction(),
                        "The action was not applyable on the model");
                logger.error("Action was not applyable: " + ae.getAction());
                synchronized (locker) {
                    locker.notify();
                }
            } else {
                if (unblocked.isEmpty()) {
                    if (rpe.getNbCommitted() == nbActions) {
                        //End of the reconfiguration
                        logger.debug("End of the reconfiguration");
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
        }
    }

    private void executeInParallel(final Action a) {
        logger.debug("Start action :{}", a);
        ActionExecutor ae = new ActionExecutor(executor, a);
        ae.start();
    }

    public class ActionExecutor extends Thread {

        private Action action;

        private boolean succeed;

        private ActionVisitor executor;

        public ActionExecutor(ActionVisitor executor, Action a) {
            action = a;
            succeed = false;
            this.executor = executor;
        }

        @Override
        public void run() {
            if (action.visit(executor) != null) {
                succeed = true;
                commit(this);
            }
        }

        public boolean succeeded() {
            return succeed;
        }

        public Action getAction() {
            return action;
        }
    }
}
