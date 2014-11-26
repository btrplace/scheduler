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

package org.btrplace.model.constraint;

import org.btrplace.plan.event.*;

/**
 * Created by vkherbac on 01/09/14.
 */
public class NoDelayChecker extends AllowAllConstraintChecker<NoDelay> {
    /**
     * Make a new checker.
     *
     * @param nd the constraint associated to the checker.
     */
    public NoDelayChecker(NoDelay nd) {
        super(nd);
    }

    @Override
    public boolean start(MigrateVM a) {
        if (getVMs().contains(a.getVM())) {
            if (a.getStart() == 0) {
                return startRunningVMPlacement(a);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean start(BootVM a) {
        if (getVMs().contains(a.getVM())) {
            if (a.getStart() == 0) {
                return startRunningVMPlacement(a);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean start(ShutdownVM a) {
        if (getVMs().contains(a.getVM())) {
            return (a.getStart() == 0);
        }
        return true;
    }

    @Override
    public boolean start(ResumeVM a) {
        if (getVMs().contains(a.getVM())) {
            if (a.getStart() == 0) {
                return startRunningVMPlacement(a);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean start(SuspendVM a) {
        if (getVMs().contains(a.getVM())) {
            return (a.getStart() == 0);
        }
        return true;
    }

    @Override
    public boolean start(KillVM a) {
        if (getVMs().contains(a.getVM())) {
            return (a.getStart() == 0);
        }
        return true;
    }

    @Override
    public boolean start(ForgeVM a) {
        if (getVMs().contains(a.getVM())) {
            return (a.getStart() == 0);
        }
        return true;
    }
}
