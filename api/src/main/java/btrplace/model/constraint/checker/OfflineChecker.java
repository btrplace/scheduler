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
import btrplace.model.constraint.Offline;
import btrplace.plan.event.BootNode;

import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Offline} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Offline
 */
public class OfflineChecker extends AllowAllConstraintChecker<Offline> {

    /**
     * Make a new checker.
     *
     * @param o the associated constraint
     */
    public OfflineChecker(Offline o) {
        super(o);
    }

    @Override
    public boolean start(BootNode a) {
        return !getNodes().contains(a.getNode());
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (UUID n : getNodes()) {
            if (!c.getOfflineNodes().contains(n)) {
                return false;
            }
        }
        return true;
    }
}
