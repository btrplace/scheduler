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

package org.btrplace.safeplace.guard;

import org.btrplace.safeplace.CTestCaseResult;
import org.btrplace.safeplace.verification.TestCase;

/**
 * @author Fabien Hermenier
 */
public class TimeGuard implements Guard {

    private int d;

    private long start = -1;

    public TimeGuard(int s) {
        d = s;
    }

    @Override
    public boolean acceptDefiant(TestCase tc) {
        return checkTimer();
    }

    @Override
    public boolean acceptCompliant(TestCase tc) {
        return checkTimer();
    }

    private boolean checkTimer() {
        if (start == -1) {
            start = System.currentTimeMillis();
        }
        return System.currentTimeMillis() < start + d * 1000;
    }

    @Override
    public boolean accept(CTestCaseResult r) {
        return checkTimer();
    }
}
