/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.safeplace.testing.verification.btrplace;

import org.btrplace.model.Element;
import org.btrplace.model.constraint.AllowAllConstraintChecker;

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
