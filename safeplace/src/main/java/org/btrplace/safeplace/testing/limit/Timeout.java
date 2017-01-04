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
 * A predicate to cap the testing duration.
 * @author Fabien Hermenier
 */
public class Timeout implements Predicate<Result> {

    private int max;
    private long start;

    /**
     * New threshold.
     *
     * @param max the testing duration in seconds
     */
    public Timeout(int max) {
        this.max = max;
        start = -1;
    }

    @Override
    public boolean test(Result tc) {
        if (start == -1) {
            start = System.currentTimeMillis();
        }

        return System.currentTimeMillis() - start < max * 1000;
    }

    @Override
    public String toString() {
        return "duration <= " + max + " sec.";
    }

}
