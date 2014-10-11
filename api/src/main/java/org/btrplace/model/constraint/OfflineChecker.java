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
import org.btrplace.model.Node;
import org.btrplace.plan.event.BootNode;

/**
 * Checker for the {@link org.btrplace.model.constraint.Offline} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Offline
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
        for (Node n : getNodes()) {
            if (!c.isOffline(n)) {
                return false;
            }
        }
        return true;
    }
}
