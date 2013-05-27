/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Sleeping;
import btrplace.plan.event.SuspendVM;

;

/**
 * Checker for the {@link btrplace.model.constraint.Sleeping} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Sleeping
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
        for (int vm : getVMs()) {
            if (!c.getSleepingVMs().contains(vm)) {
                return false;
            }
        }
        return true;
    }
}
