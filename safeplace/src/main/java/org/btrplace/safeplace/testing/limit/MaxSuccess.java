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
import org.btrplace.safeplace.testing.TestCaseResult;

import java.util.function.Predicate;

/**
 * @author Fabien Hermenier
 */
public class MaxSuccess implements Predicate<TestCaseResult> {

    private int max;
    public MaxSuccess(int max) {
        this.max = max;
    }

    @Override
    public boolean test(TestCaseResult tc) {
        if (tc.result() == Result.success) {
            max--;
        }
        return max != 0;
    }

    @Override
    public String toString() {
        return "successes < " + max;
    }

}
