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
