/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing;

import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.spec.SpecScanner;
import org.testng.Assert;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TestScannerTest {

    //@Test
    public void testBench() throws Exception {
        TestScanner sc = newScanner();
        List<TestCampaign> campaigns = sc.testGroups("noVMsOnOfflineNodes");
        if (campaigns.isEmpty()) {
            Assert.fail("Nothing to test");
        }
        Assert.assertEquals(campaigns.stream().mapToInt(tc -> {
            tc.limits().tests(1000);
            return tc.go().defects();
        }).sum(), 0);
    }


    public TestScanner newScanner() throws Exception {
        SpecScanner specScanner = new SpecScanner();
        List<Constraint> l = specScanner.scan();
        return new TestScanner(l);
    }
}