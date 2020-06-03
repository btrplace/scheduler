/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing;

import org.btrplace.safeplace.testing.fuzzer.ConfigurableFuzzer;
import org.btrplace.safeplace.testing.fuzzer.Restriction;
import org.btrplace.safeplace.testing.fuzzer.decorators.ShareableResourceFuzzer;
import org.btrplace.safeplace.testing.reporting.Counting;
import org.btrplace.safeplace.testing.reporting.Report;
import org.btrplace.safeplace.testing.verification.Verifier;
import org.btrplace.safeplace.testing.verification.spec.SpecVerifier;

import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
@SuppressWarnings("squid:S106")
public class Bench {

    public static int scale = 1;

    public static int population = 100;
    public static boolean transitions = true;

    public enum Mode {SAVE, REPLAY, DEFAULT}

    public static String source = ".";
    public static Mode mode = Mode.DEFAULT;

    public static Set<Restriction> restrictions = EnumSet.allOf(Restriction.class);
    public static Report report = new Counting();

    public static TestCampaign thousand(TestCampaign tc, String cstr) {
        return thousand(tc, cstr, new SpecVerifier());
    }

    public static TestCampaign thousand(TestCampaign tc, String cstr, Verifier v) {

        tc.reportTo(report);
        tc.verifyWith(v);

        if (mode == Mode.REPLAY) {
            tc.replay(Paths.get(source, cstr + ".json"));
            return tc;
        }
        tc.printProgress(true);
        tc.limits().tests(population);
        ConfigurableFuzzer f = tc.check(cstr).restriction(EnumSet.allOf(Restriction.class));
        f.restriction(restrictions);
        if (transitions) {
            f.vms(scale).nodes(scale).srcOffNodes(0.1).srcVMs(30, 70, 0).dstVMs(30, 70, 0);
        } else {
            f.vms(scale).nodes(scale).srcOffNodes(0).dstOffNodes(0).srcVMs(0, 1, 0).dstVMs(0, 1, 0);
        }
        f.with("nb", 1, 10);

        if (mode == Mode.SAVE) {
            f.save(Paths.get(source, cstr + ".json").toString());
        }
        return tc;
    }

    @CstrTest(groups = {"core","noVMsOnOfflineNodes"})
    public void testNoVmsOnOfflineNodes(TestCampaign c) {
        thousand(c, "noVMsOnOfflineNodes");
    }

    @CstrTest(groups = {"core","toRunning"})
    public void testToRunning(TestCampaign c) {
        thousand(c, "toRunning");
    }

    @CstrTest(groups = {"core"})
    public void testToSleeping(TestCampaign c) {
        thousand(c, "toSleeping");
    }

    @CstrTest(groups = {"core"})
    public void testToReady(TestCampaign c) {
        thousand(c, "toReady");
    }

    @CstrTest(groups = {"vm-vm","spread", "sides","bi"})
    public void testSpread(TestCampaign c) {
        thousand(c,"spread");
    }

    @CstrTest(groups = {"vm-vm", "sides","bi","lonely"})
    public void testLonely(TestCampaign c) {
        thousand(c,"lonely");
    }

    @CstrTest(groups = {"vm-vm", "sides","bi"})
    public void testGather(TestCampaign c) {
        thousand(c,"gather");
    }

    @CstrTest(groups = {"vm-pm", "sides","bi","ban"})
    public void testBan(TestCampaign c) {
        thousand(c,"ban");
    }

    @CstrTest(groups = {"vm-pm", "sides","bi","fence"})
    public void testFence(TestCampaign c) {
        thousand(c,"fence");
    }

  /*    @CstrTest(groups = {"vm-vm", "sides","bi"})
     public void testAmong(TestCampaign c) {
          thousand(c,"among");
      }
  */
    @CstrTest(groups = {"vm-pm", "sides", "root"})
    public void testRoot(TestCampaign c) {
        thousand(c,"root");
    }

    @CstrTest(groups = {"vm-vm", "sides","bi"})
    public void testSplit(TestCampaign c) {
        thousand(c, "split");
    }

    @CstrTest(groups = {"vm-pm", "sides"})
    public void testQuarantine(TestCampaign c) {
        thousand(c, "quarantine");
    }

    @CstrTest(groups = {"counting", "sides","bi"})
    public void testMaxOnline(TestCampaign c) {
        thousand(c, "maxOnline");
    }

    @CstrTest(groups = {"counting", "sides","bi"})
    public void testRunningCapacity(TestCampaign c) {
        thousand(c, "runningCapacity");
    }

    @CstrTest(groups = {"state", "sides","running"})
    public void testRunning(TestCampaign c) {
        thousand(c, "running");
    }

    @CstrTest(groups = {"state", "sides","sleeping"})
    public void testSleeping(TestCampaign c) {
        thousand(c, "sleeping");
    }

    @CstrTest(groups = {"state", "sides"})
    public void testReady(TestCampaign c) {
        thousand(c, "ready");
    }

    @CstrTest(groups = {"state", "sides", "ONLINE"})
    public void testOnline(TestCampaign c) {
        thousand(c, "ONLINE");
    }

    @CstrTest(groups = {"state", "sides"})
    public void testOffline(TestCampaign c) {
        thousand(c, "OFFLINE");
    }

    @CstrTest(groups = {"resource", "sides", "rc"})
    public void testResource(TestCampaign c) {
        thousand(c, "shareableresource");
        if (Bench.mode != Mode.REPLAY) {
            c.check("shareableresource").with("id", "cpu")
                .with(new ShareableResourceFuzzer("cpu", 1, 5, 10, 20).variability(0.5));
        }
    }

    @CstrTest(groups = {"resource", "sides", "capacity"})
    public void testResourceCapacity(TestCampaign c) {
        thousand(c, "resourceCapacity");
        if (Bench.mode != Mode.REPLAY) {
            c.check("resourceCapacity").with("id", "cpu")
                    .with("qty", 1, 5)
                    .with(new ShareableResourceFuzzer("cpu", 1, 5, 10, 20).variability(1));
        }
    }
}
