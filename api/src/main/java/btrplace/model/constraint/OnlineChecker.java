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

package btrplace.model.constraint;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.plan.event.ShutdownNode;

/**
 * Checker for the {@link btrplace.model.constraint.Online} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Online
 */
public class OnlineChecker extends AllowAllConstraintChecker<Online> {

    /**
     * Make a new checker.
     *
     * @param o the associated constraint
     */
    public OnlineChecker(Online o) {
        super(o);
    }

    @Override
    public boolean start(ShutdownNode a) {
        return !getNodes().contains(a.getNode());
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (Node n : getNodes()) {
            if (!c.isOnline(n)) {
                return false;
            }
        }
        return true;
    }
}
