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
            tc.fuzz().vms(5).nodes(5);
            tc.reporting().verbosity(2);
            tc.limits().tests(1000);
            return tc.go();
        }).sum(), 0);
    }


    public TestScanner newScanner() throws Exception {
        SpecScanner specScanner = new SpecScanner();
        List<Constraint> l = specScanner.scan();
        return new TestScanner(l);
    }
}