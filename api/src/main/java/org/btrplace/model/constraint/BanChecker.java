/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.plan.event.RunningVMPlacement;

/**
 * Checker for the {@link Ban} constraint
 *
 * @author Fabien Hermenier
 * @see Ban
 */
public class BanChecker extends AllowAllConstraintChecker<Ban> {

    /**
     * Make a new checker.
     *
     * @param b the associated constraint
     */
    public BanChecker(Ban b) {
        super(b);
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement r) {
        if (getVMs().contains(r.getVM())) {
            return !getNodes().contains(r.getDestinationNode());
        }
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (VM vm : getVMs()) {
            if (c.isRunning(vm) && getNodes().contains(c.getVMLocation(vm))) {
                return false;
            }
        }
        return true;
    }
}
