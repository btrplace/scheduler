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
import btrplace.safeplace.runner.CTestCasesRunner;
import btrplace.safeplace.verification.spec.IntVerifDomain;

/**
 * @author Fabien Hermenier
 */
public class TestMaxOnline {

    @CstrTest(constraint = "maxOnline", groups = {"counting", "unit"})
    public void testContinuous(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous()).dom(new IntVerifDomain(0, 5));
    }

    @CstrTest(constraint = "maxOnline", groups = {"counting", "unit"})
    public void testContinuousRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous()).dom(new IntVerifDomain(0, 5)).impl().repair(true);
    }

    @CstrTest(constraint = "maxOnline", groups = {"counting", "unit"})
    public void testDiscrete(CTestCasesRunner r) {
        TestUtils.quickCheck(r.discrete()).dom(new IntVerifDomain(0, 5));
    }

    @CstrTest(constraint = "maxOnline", groups = {"counting", "unit"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(r.discrete()).dom(new IntVerifDomain(0, 5)).impl().repair(true);
        ;
    }
}
