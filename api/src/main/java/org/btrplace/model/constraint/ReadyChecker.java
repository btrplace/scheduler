/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.plan.event.ForgeVM;
import org.btrplace.plan.event.ShutdownVM;

/**
 * Checker for the {@link org.btrplace.model.constraint.Ready} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Ready
 */
public class ReadyChecker extends DenyMyVMsActions<Ready> {

    /**
     * Make a new checker.
     *
     * @param r the associated constraint
     */
    public ReadyChecker(Ready r) {
        super(r);
    }

    @Override
    public boolean start(ForgeVM a) {
        return true;
    }

    @Override
    public boolean start(ShutdownVM a) {
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (VM vm : getVMs()) {
            if (!c.isReady(vm)) {
                return false;
            }
        }
        return true;
    }
}
