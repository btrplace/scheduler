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
import btrplace.model.constraint.Ready;
import btrplace.plan.event.ForgeVM;
import btrplace.plan.event.ShutdownVM;

;

/**
 * Checker for the {@link btrplace.model.constraint.Ready} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Ready
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
        for (int vm : getVMs()) {
            if (!c.getReadyVMs().contains(vm)) {
                return false;
            }
        }
        return true;
    }
}
