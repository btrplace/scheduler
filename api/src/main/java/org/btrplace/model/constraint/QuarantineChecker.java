/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.RunningVMPlacement;

/**
 * Checker for the {@link org.btrplace.model.constraint.Quarantine} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Quarantine
 */
public class QuarantineChecker extends AllowAllConstraintChecker<Quarantine> {

    /**
     * Make a new checker.
     *
     * @param q the associated constraint
     */
    public QuarantineChecker(Quarantine q) {
        super(q);
    }

    @Override
    public boolean start(MigrateVM a) {
        if (getVMs().contains(a.getVM())) {
            //the VM can not move elsewhere
            return false;
        }
        return startRunningVMPlacement(a);
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        return !getNodes().contains(a.getDestinationNode());
    }

    @Override
    public boolean endsWith(Model mo) {
        return true;
    }

    @Override
    public boolean startsWith(Model mo) {
        Mapping map = mo.getMapping();
        getVMs().clear();
        return getVMs().addAll(map.getRunningVMs(getNodes()));
    }

}
