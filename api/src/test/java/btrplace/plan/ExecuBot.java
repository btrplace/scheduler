package btrplace.plan;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class ExecuBot {

    public void execute(ReconfigurationPlanMonitor exe) {

        while (!exe.isOver()) {
            Set<Action> feasible = exe.getFeasibleActions();
            for (Action a : feasible) {
                new ActionExecutor(a, exe).start();
            }
        }
    }

    public static class ActionExecutor extends Thread {

        Action a;
        ReconfigurationPlanMonitor executor;

        public ActionExecutor(Action a, ReconfigurationPlanMonitor exec) {
            this.a = a;
            this.executor = exec;

        }

        @Override
        public void run() {
            try {
                Thread.sleep((long) (Math.random() * 1000));
                executor.commit(a);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
