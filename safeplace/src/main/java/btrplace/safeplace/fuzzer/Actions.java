/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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

package btrplace.safeplace.fuzzer;

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
        } else if (a instanceof KillVM) {
            KillVM m = (KillVM) a;
            return new KillVM(m.getVM(), ((KillVM) a).getNode(), st, ed);
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
