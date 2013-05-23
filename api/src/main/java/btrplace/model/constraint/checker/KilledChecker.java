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
import btrplace.model.constraint.Killed;
import btrplace.plan.event.KillVM;

import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Killed} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Killed
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
        for (UUID vm : getVMs()) {
            if (c.getAllVMs().contains(vm)) {
                return false;
            }
        }
        return true;
    }
}
