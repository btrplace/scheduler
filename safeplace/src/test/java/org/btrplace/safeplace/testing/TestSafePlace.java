/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing;

import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.spec.SpecScanner;
import org.btrplace.safeplace.testing.fuzzer.Restriction;
import org.btrplace.safeplace.testing.fuzzer.decorators.ShareableResourceFuzzer;
import org.testng.Assert;

import java.util.EnumSet;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TestSafePlace {

  //Core constraints
  @CstrTest(groups = {"core_"})
  public void testNoVmsOnOfflineNodes(TestCampaign c) {
    c.check("noVMsOnOfflineNodes")
            .vms(1)
            .nodes(1)
            .srcOffNodes(0.1)
            .srcVMs(1, 9, 0);
  }

  @CstrTest(groups = {"core_", "toRunning"})
  public void testToRunning(TestCampaign c) {
    c.check("toRunning").vms(1).nodes(1).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest(groups = {"core_"})
  public void testToSleeping(TestCampaign c) {
    c.check("toSleeping").vms(1).nodes(1).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest(groups = {"core_"})
  public void testToReady(TestCampaign c) {
    c.check("toReady").vms(1).nodes(1).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }


  @CstrTest(groups = {"spread"})
  public void testSpread(TestCampaign c) {
    c.check("spread").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest()
  public void testLonely(TestCampaign c) {
    c.check("lonely").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest(groups = {"gather"})
  public void testGather(TestCampaign c) {
    c.check("gather").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest(groups = {"ban"})
  public void testBan(TestCampaign c) {
    c.check("ban").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest(groups = {"fence",})
  public void testFence(TestCampaign c) {
    c.check("fence").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest(groups = {"among"})
  public void testAmong(TestCampaign c) {
    c.check("among").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest()
  public void testRoot(TestCampaign c) {
    c.check("root").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0);
    c.limits().tests(100).failures(1);
    //c.onDefect(DefectHooks.testNgFailure);
  }

  @CstrTest()
  public void testSplit(TestCampaign c) {
    c.check("split").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest()
  public void testQuarantine(TestCampaign c) {
    c.check("quarantine").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest(groups = {"maxOnline"})
  public void testMaxOnline(TestCampaign c) {
    c.check("maxOnline").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0)
            .with("nb", 0, 5);
  }

  @CstrTest(groups = {"runningCapacity"})
  public void testRunningCapacity(TestCampaign c) {
    c.check("runningCapacity").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0)
            .with("nb", 0, 12);
  }

  @CstrTest()
  public void testRunning(TestCampaign c) {
    c.check("running").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest()
  public void testSleeping(TestCampaign c) {
    c.check("sleeping").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest()
  public void testReady(TestCampaign c) {
    c.check("ready").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest()
  public void testOnline(TestCampaign c) {
    c.check("online").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest()
  public void testOffline(TestCampaign c) {
    c.check("offline").vms(10).nodes(3).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }


  @CstrTest(groups = {})
  public void testResource(TestCampaign c) {
    c.check("shareableResource")
            .with("id", "cpu")
            .with(new ShareableResourceFuzzer("cpu", 1, 5, 10, 20).variability(0.5))
            .vms(5).nodes(1).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  @CstrTest(groups = {"_resourceCapacity"})
  public void testResourceCapacity(TestCampaign c) {
    c.check("resourceCapacity")
            .restriction(EnumSet.of(Restriction.CONTINUOUS, Restriction.DISCRETE))
            .with("id", "cpu")
            .with("qty", 1, 50)
            .with(new ShareableResourceFuzzer("cpu", 1, 5, 10, 20).variability(0.5))
            .vms(5).nodes(2).srcOffNodes(0.1).srcVMs(1, 9, 0);
  }

  //@Test
  public void launcher() throws Exception {
    SpecScanner specScanner = new SpecScanner();
    List<Constraint> l = specScanner.scan();
    TestScanner sc = new TestScanner(l);
    List<TestCampaign> campaigns = sc.test(TestSafePlace.class);
    //List<TestCampaign> campaigns = sc.testGroups("_resourceCapacity");
    if (campaigns.isEmpty()) {
      Assert.fail("Nothing to test");
    }

    campaigns.forEach(tc -> {
      tc.schedulerParams().doRepair(false);//.setVerbosity(1);
      tc.onDefect(DefectHooks.ignore);
      tc.limits().clear().tests(1000);
      System.out.println(tc.go());
    });
  }
}
