/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.plan.event.SuspendVM;

/**
 * Checker for the {@link org.btrplace.model.constraint.Sleeping} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Sleeping
 */
public class SleepingChecker extends DenyMyVMsActions<Sleeping> {

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SleepingChecker(Sleeping s) {
        super(s);
    }

    @Override
    public boolean start(SuspendVM a) {
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (VM vm : getVMs()) {
            if (!c.isSleeping(vm)) {
                return false;
            }
        }
        return true;
    }
}
