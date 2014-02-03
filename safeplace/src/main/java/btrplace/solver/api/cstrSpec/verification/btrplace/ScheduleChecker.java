package btrplace.solver.api.cstrSpec.verification.btrplace;

import btrplace.model.Element;
import btrplace.model.constraint.checker.AllowAllConstraintChecker;
import btrplace.plan.event.*;

/**
 * @author Fabien Hermenier
 */
public class ScheduleChecker extends AllowAllConstraintChecker<Schedule> {

    private Element e;

    public ScheduleChecker(Schedule c) {
        super(c);
        if (c.getVM() == null) {
            e = c.getNode();
        } else {
            e = c.getVM();
        }
    }

    @Override
    public boolean start(MigrateVM a) {
        return check(a);
    }

    private boolean check(Action a) {
        if (a instanceof VMEvent) {
            VMEvent a2 = (VMEvent) a;
            if (a2.getVM().equals(e)) {
                return getConstraint().getStart() == a.getStart() && getConstraint().getEnd() == a.getEnd();
            }
        } else if (a instanceof NodeEvent) {
            NodeEvent a2 = (NodeEvent) a;
            if (a2.getNode().equals(e)) {
                return getConstraint().getStart() == a.getStart() && getConstraint().getEnd() == a.getEnd();
            }
        }

        return true;
    }

    @Override
    public boolean start(BootVM a) {
        return check(a);
    }

    @Override
    public boolean start(ShutdownVM a) {
        return check(a);
    }

    @Override
    public boolean start(ResumeVM a) {
        return check(a);
    }

    @Override
    public boolean start(SuspendVM a) {
        return check(a);
    }

    @Override
    public boolean start(KillVM a) {
        return check(a);
    }

    @Override
    public boolean start(ForgeVM a) {
        return check(a);
    }

    @Override
    public boolean start(Allocate e) {
        return check(e);
    }
}
