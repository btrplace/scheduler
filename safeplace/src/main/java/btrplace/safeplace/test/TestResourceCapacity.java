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

import btrplace.safeplace.annotations.CstrTest;
import btrplace.safeplace.fuzzer.ShareableResourceFuzzer;
import btrplace.safeplace.runner.CTestCasesRunner;
import btrplace.safeplace.verification.spec.IntVerifDomain;
import btrplace.safeplace.verification.spec.StringEnumVerifDomain;

/**
 * @author Fabien Hermenier
 */
public class TestResourceCapacity {

    private static String[] rcDoms = new String[]{"cpu", "mem"};

    private ShareableResourceFuzzer rcf = new ShareableResourceFuzzer("cpu", 1, 7, 3, 5);

    private static CTestCasesRunner customize(CTestCasesRunner r) {
        r.dom(new StringEnumVerifDomain(rcDoms))
                .dom(new IntVerifDomain(1, 7));
        return r;
    }
    /*@CstrTest(constraint = "resourceCapacity", groups = {"rc", "unit"})
    public void testContinuous(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.continuous()));
    }

    @CstrTest(constraint = "resourceCapacity", groups = {"rc", "unit"})
    public void testContinuousRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.continuous())).impl().repair(true);
    }       */

    @CstrTest(constraint = "resourceCapacity", groups = {"rc", "unit"})
    public void testDiscrete(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.discrete()));
    }

    @CstrTest(constraint = "resourceCapacity", groups = {"rc", "unit"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.discrete())).impl().repair(true);
    }

}
