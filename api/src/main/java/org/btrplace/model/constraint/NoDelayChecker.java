/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.ForgeVM;
import org.btrplace.plan.event.KillVM;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ResumeVM;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SuspendVM;

/**
 * Checker for the {@link org.btrplace.model.constraint.NoDelay} constraint
 *
 * @author Vincent Kherbache
 * @see org.btrplace.model.constraint.NoDelay
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
            return a.getStart() == 0;
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
            return a.getStart() == 0;
        }
        return true;
    }

    @Override
    public boolean start(KillVM a) {
        if (getVMs().contains(a.getVM())) {
            return a.getStart() == 0;
        }
        return true;
    }

    @Override
    public boolean start(ForgeVM a) {
        if (getVMs().contains(a.getVM())) {
            return a.getStart() == 0;
        }
        return true;
    }
}
