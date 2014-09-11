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

package btrplace.safeplace.test;

import btrplace.safeplace.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestUtils {

    public static final CTestCasesRunner quickCheck(CTestCasesRunner r) {
        r.timeout(30);
        r.maxTests(100000);
        //r.maxFailures(1);
        return r;
    }

    public static final CTestCasesRunner longCheck(CTestCasesRunner r) {
        r.maxTests(100);
        r.timeout(10);
        //r.maxFailures(1);
        return r;
    }
}
