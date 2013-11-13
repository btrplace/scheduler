package btrplace.solver.api.cstrSpec.generator;

import btrplace.plan.event.*;

/**
 * @author Fabien Hermenier
 */
public class Actions {

    public static Action newAction(Action a, int st, int ed) {
        if (a instanceof MigrateVM) {
            MigrateVM m = (MigrateVM) a;
            return new MigrateVM(m.getVM(), m.getSourceNode(), m.getDestinationNode(), st, ed);
        } else if (a instanceof BootVM) {
            BootVM m = (BootVM) a;
            return new BootVM(m.getVM(), m.getDestinationNode(), st, ed);
        } else if (a instanceof ShutdownVM) {
            ShutdownVM m = (ShutdownVM) a;
            return new ShutdownVM(m.getVM(), m.getNode(), st, ed);
        } else if (a instanceof BootNode) {
            BootNode m = (BootNode) a;
            return new BootNode(m.getNode(), st, ed);
        } else if (a instanceof ShutdownNode) {
            ShutdownNode m = (ShutdownNode) a;
            return new ShutdownNode(m.getNode(), st, ed);
        } else if (a instanceof SuspendVM) {
            SuspendVM m = (SuspendVM) a;
            return new SuspendVM(m.getVM(), m.getSourceNode(), m.getDestinationNode(), st, ed);
        } else if (a instanceof ResumeVM) {
            ResumeVM m = (ResumeVM) a;
            return new ResumeVM(m.getVM(), m.getSourceNode(), m.getDestinationNode(), st, ed);
        } else {
            throw new UnsupportedOperationException("Unsupported action '" + a + "'");
        }
    }

    public static Action newDelay(Action a, int d) {
        return newAction(a, d, a.getEnd() - a.getStart() + d);
    }

    public static Action newDuration(Action a, int d) {
        return newAction(a, a.getStart(), a.getStart() + d);
    }
}
