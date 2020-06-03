/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.plan.event.AllocateEvent;
import org.btrplace.plan.event.RunningVMPlacement;

/**
 * Checker for the {@link org.btrplace.model.constraint.Running} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Running
 */
public class RunningChecker extends DenyMyVMsActions<Running> {

    /**
     * Make a new checker.
     *
     * @param r the associated constraint
     */
    public RunningChecker(Running r) {
        super(r);
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        return true;
    }

    @Override
    public boolean consume(AllocateEvent e) {
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (VM vm : getVMs()) {
            if (!c.isRunning(vm)) {
                return false;
            }
        }
        return true;
    }
}
