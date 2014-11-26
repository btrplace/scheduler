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
