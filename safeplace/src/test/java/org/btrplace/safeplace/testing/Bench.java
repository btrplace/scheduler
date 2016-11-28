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

import org.btrplace.safeplace.testing.fuzzer.Replay;
import org.btrplace.safeplace.testing.fuzzer.Restriction;
import org.btrplace.safeplace.testing.fuzzer.decorators.ShareableResourceFuzzer;
import org.btrplace.safeplace.testing.reporting.DefaultReporting;
import org.btrplace.safeplace.testing.reporting.Reporting;
import org.btrplace.safeplace.testing.verification.Verifier;
import org.btrplace.safeplace.testing.verification.spec.SpecVerifier;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.EnumSet;

/**
 * @author Fabien Hermenier
 */
public class Bench {

    public static int scale = 1;

    public static int population = 100;
    public static boolean transitions = true;

    public enum Mode {SAVE, REPLAY, DEFAULT};

    public static String source = ".";
    public static Mode mode = Mode.DEFAULT;

    public static Reporting reporting = new DefaultReporting().verbosity(3).capture(x -> false);

    public static TestCampaign thousand(TestCampaign tc, String cstr) {
        return thousand(tc, cstr, new SpecVerifier());
    }

    public static TestCampaign thousand(TestCampaign tc, String cstr, Verifier v) {

        tc.reporting(reporting.verbosity(1));
        tc.verifyWith(v);

        if (mode == Mode.REPLAY) {
            try {
                tc.fuzzer(new Replay(Paths.get(source, cstr + ".json")));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            return tc;
        }
        tc.limits().tests(population);
        tc.constraint(cstr);
        tc.fuzz().restriction(EnumSet.allOf(Restriction.class));
        if (transitions) {
            tc.fuzz().vms(scale).nodes(scale).srcOffNodes(0.1).srcVMs(0.3, 0.7, 0).dstVMs(0.3, 0.7, 0);
        } else {
            tc.fuzz().vms(scale).nodes(scale).srcOffNodes(0).dstOffNodes(0).srcVMs(0, 1, 0).dstVMs(0, 1, 0);
        }
        tc.fuzz().with("nb", 1, 10);

        if (mode == Mode.SAVE) {
            tc.save(Paths.get(source, cstr + ".json").toString());
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

    @CstrTest(groups = {"vm-vm", "sides","bi"})
    public void testAmong(TestCampaign c) {
        thousand(c,"among");
    }

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

    @CstrTest(groups = {"state", "sides","online"})
    public void testOnline(TestCampaign c) {
        thousand(c, "online");
    }

    @CstrTest(groups = {"state", "sides"})
    public void testOffline(TestCampaign c) {
        thousand(c, "offline");
    }

    @CstrTest(groups = {"resource", "sides", "rc"})
    public void testResource(TestCampaign c) {
        thousand(c, "shareableresource");
        if (Bench.mode != Mode.REPLAY) {
                c.fuzz().with("id", "cpu")
                .with(new ShareableResourceFuzzer("cpu", 1, 5, 10, 20).variability(0.5));
        }
    }

    @CstrTest(groups = {"resource", "sides", "capacity"})
    public void testResourceCapacity(TestCampaign c) {
        thousand(c, "resourceCapacity");
        if (Bench.mode != Mode.REPLAY) {
            c.fuzz().with("id", "cpu")
                    .with("qty", 1, 5)
                    .with(new ShareableResourceFuzzer("cpu", 1, 5, 10, 20).variability(1));
        }
    }
}
