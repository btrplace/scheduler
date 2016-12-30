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

package org.btrplace.safeplace.testing.limit;

import org.btrplace.safeplace.testing.Result;

import java.util.function.Predicate;

/**
 * A predicate to limit the number of defects.
 * @author Fabien Hermenier
 */
public class MaxDefects implements Predicate<Result> {

    private int max;

    /**
     * New threshold.
     *
     * @param max the maximum number of defects
     */
    public MaxDefects(int max) {
        this.max = max;
    }

    @Override
    public boolean test(Result tc) {
        if (tc != Result.success) {
            max--;
        }
        return max > 0;
    }

    @Override
    public String toString() {
        return "defects < " + max;
    }
}
