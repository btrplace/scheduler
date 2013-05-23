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
import btrplace.model.constraint.SingleResourceCapacity;
import btrplace.model.view.ShareableResource;

import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.SingleResourceCapacity} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.SingleResourceCapacity
 */
public class SingleResourceCapacityChecker extends AllowAllConstraintChecker<SingleResourceCapacity> {

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SingleResourceCapacityChecker(SingleResourceCapacity s) {
        super(s);
    }

    @Override
    public boolean endsWith(Model i) {
        ShareableResource rc = (ShareableResource) i.getView(ShareableResource.VIEW_ID_BASE + getConstraint().getResource());
        if (rc == null) {
            return false;
        }
        Mapping map = i.getMapping();
        for (UUID n : getNodes()) {
            if (rc.sum(map.getRunningVMs(n), true) > getConstraint().getAmount()) {
                return false;
            }
        }
        return true;
    }
}
