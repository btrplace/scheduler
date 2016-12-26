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
import org.btrplace.safeplace.testing.fuzzer.Restriction;
import org.btrplace.safeplace.testing.fuzzer.decorators.ShareableResourceFuzzer;
import org.testng.Assert;

import java.util.EnumSet;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Fabien Hermenier
 */
public class TestSafePlace {

    //Core constraints
    @CstrTest(groups = {"core"})
    public void testNoVmsOnOfflineNodes(TestCampaign c) {
        c.fuzz().vms(1).nodes(1).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("noVMsOnOfflineNodes");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest(groups = {"core"})
    public void testToRunning(TestCampaign c) {
        c.fuzz().vms(1).nodes(1).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("toRunning");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest(groups = {"core"})
    public void testToSleeping(TestCampaign c) {
        c.fuzz().vms(1).nodes(1).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("toSleeping");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest(groups = {"core"})
    public void testToReady(TestCampaign c) {
        c.fuzz().vms(1).nodes(1).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("toReady");
        assertEquals(c.reporting().done(), 0);
    }


    @CstrTest()
    public void testSpread(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("spread");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest()
    public void testLonely(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("lonely");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest()
    public void testGather(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("gather");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest
    public void testBan(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("ban");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest()
    public void testFence(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("fence");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest()
    public void testAmong(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("among");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest()
    public void testRoot(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("root");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest()
    public void testSplit(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("split");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest()
    public void testQuarantine(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("quarantine");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest()
    public void testMaxOnline(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("maxOnline");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest()
    public void testRunningCapacity(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("runningCapacity");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest()
    public void testRunning(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("running");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest()
    public void testSleeping(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("sleeping");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest()
    public void testReady(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("ready");
        assertEquals(c.reporting().done(), 0);

    }

    @CstrTest()
    public void testOnline(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("online");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest()
    public void testOffline(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.limits().tests(100).failures(1);
        c.constraint("offline");
        assertEquals(c.reporting().done(), 0);
    }


    @CstrTest
    public void testResource(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.fuzz().with("id","cpu");
        c.fuzz().with(new ShareableResourceFuzzer("cpu", 1, 5, 10, 20).variability(0.5));
        c.limits().tests(100).failures(1);
        c.constraint("shareableResource");
        assertEquals(c.reporting().done(), 0);
    }

    @CstrTest(groups = {"resourceCapacity"})
    public void testResourceCapacity(TestCampaign c) {
        c.fuzz().vms(10).nodes(3).srcOffNodes(0.1).srcVMs(0.1, 0.9, 0);
        c.fuzz().with("id","cpu")
                  .with("qty",1, 50)
                  .with(new ShareableResourceFuzzer("cpu", 1, 5, 10, 20).variability(0.5));
        c.limits().tests(100).failures(1);
        c.constraint("resourceCapacity");
        c.fuzz().restriction(EnumSet.of(Restriction.continuous, Restriction.discrete));
        c.reporting().capture(reporting -> reporting.result() != Result.success);
        assertEquals(c.reporting().done(), 0);
    }

    //@Test
    public void launcher() throws Exception {
        SpecScanner specScanner = new SpecScanner();
        List<Constraint> l = specScanner.scan();
        TestScanner sc = new TestScanner(l);
        List<TestCampaign> campaigns = sc.testGroups("resourceCapacity");
        if (campaigns.isEmpty()) {
            Assert.fail("Nothing to test");
        }
        assertEquals(campaigns.stream().mapToInt(tc -> {tc.schedulerParams().doRepair(false); return tc.go();}).sum(), 0);

    }
    /*
    preserve
    resourceCapacity
    overbook

    noDelay
    precedence
    seq
     */
}
