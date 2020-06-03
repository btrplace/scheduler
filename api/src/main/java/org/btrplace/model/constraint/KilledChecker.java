/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.plan.event.KillVM;

/**
 * Checker for the {@link org.btrplace.model.constraint.Killed} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Killed
 */
public class KilledChecker extends DenyMyVMsActions<Killed> {

    /**
     * Make a new checker.
     *
     * @param k the associated constraint
     */
    public KilledChecker(Killed k) {
        super(k);
    }

    @Override
    public boolean start(KillVM a) {
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (VM vm : getVMs()) {
            if (c.getAllVMs().contains(vm)) {
                return false;
            }
        }
        return true;
    }
}
