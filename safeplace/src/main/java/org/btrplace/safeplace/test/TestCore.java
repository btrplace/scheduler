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

package org.btrplace.safeplace.test;

import org.btrplace.safeplace.annotations.CstrTest;
import org.btrplace.safeplace.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestCore {

    @CstrTest(constraint = "noVMsOnOfflineNodes", groups = {"core", "unit"})
    public void testNoVMsOnOfflineNodes(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous());
    }

    @CstrTest(constraint = "toRunning", groups = {"core", "unit"})
    public void testToRunning(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous());
    }

    @CstrTest(constraint = "toSleeping", groups = {"core", "unit"})
    public void testToSleeping(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous());
    }

    @CstrTest(constraint = "toReady", groups = {"core", "unit"})
    public void testToReady(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous());
    }

}
