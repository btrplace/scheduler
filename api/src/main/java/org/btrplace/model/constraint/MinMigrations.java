/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

/**
 * An objective that just minimizes the cumulative duration of the migrations
 * to perform during a reconfiguration.
 *
 * Contrarily to {@link MinMTTR}, it only focuses on migration and do not consider
 * the possible action delay.
 *
 * @author Fabien Hermenier
 */
public class MinMigrations extends OptConstraint {

    @Override
    public String id() {
        return "minMigrations";
    }
}
