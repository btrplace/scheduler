/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.model.constraint.migration;

import org.btrplace.model.constraint.OptConstraint;

/**
 * An optimization constraint that minimizes the time to repair a non-viable model involving a set of migrations.
 * In practice it minimizes the sum of the ending moment for each needed actions.
 *
 * @author Vincent Kherbache
 */
public class MinMTTRMig extends OptConstraint {

    @Override
    public String id() {
        return "minimizeMTTRMigrationScheduling";
    }
}
