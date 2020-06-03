/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing.verification.btrplace;

import org.btrplace.model.Element;
import org.btrplace.model.constraint.AllowAllConstraintChecker;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.ForgeVM;
import org.btrplace.plan.event.KillVM;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.NodeEvent;
import org.btrplace.plan.event.ResumeVM;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SuspendVM;
import org.btrplace.plan.event.VMEvent;

/**
 * @author Fabien Hermenier
 */
public class ScheduleChecker extends AllowAllConstraintChecker<Schedule> {

  private final Element e;

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
