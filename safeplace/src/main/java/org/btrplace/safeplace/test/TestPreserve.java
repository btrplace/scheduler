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

import org.btrplace.json.model.view.ShareableResourceConverter;
import org.btrplace.safeplace.annotations.CstrTest;
import org.btrplace.safeplace.annotations.CstrTestsProvider;
import org.btrplace.safeplace.fuzzer.ReconfigurationPlanFuzzer2;
import org.btrplace.safeplace.fuzzer.ShareableResourceFuzzer;
import org.btrplace.safeplace.runner.CTestCasesRunner;
import org.btrplace.safeplace.verification.spec.IntVerifDomain;
import org.btrplace.safeplace.verification.spec.StringEnumVerifDomain;

/**
 * @author Fabien Hermenier
 */
public class TestPreserve {

    private ShareableResourceFuzzer rcf = new ShareableResourceFuzzer("cpu", 1, 7, 3, 5);

    @CstrTestsProvider(name = "preserve")
    public ReconfigurationPlanFuzzer2 myProvider() {
        return new ReconfigurationPlanFuzzer2().viewFuzzer(rcf, new ShareableResourceConverter());
    }

    private static CTestCasesRunner customize(CTestCasesRunner r) {
        return r.dom(new StringEnumVerifDomain(new String[]{"cpu"}))
                .dom(new IntVerifDomain(1, 7));
    }

    @CstrTest(provider = "myProvider", constraint = "preserve", groups = {"rc", "unit"})
    public void testDiscrete(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.discrete()));
    }

    @CstrTest(provider = "myProvider", constraint = "preserve", groups = {"rc", "unit"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.discrete())).impl().repair(true);
    }

    /*@CstrTest(constraint = "preserve", groups = {"rc", "unit"})
    public void testContinuous(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.continuous()));
    }

    @CstrTest(constraint = "preserve", groups = {"rc", "unit"})
    public void testContinuousRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.continuous())).impl().repair(true);
    }       */
}
