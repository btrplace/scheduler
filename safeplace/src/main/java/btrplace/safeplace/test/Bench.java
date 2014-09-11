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
public class Bench {

    public static final CTestCasesRunner check(CTestCasesRunner r) {
        r.maxFailures(1);
        r.maxTests(10);
        return r;
    }

    //Among
    @CstrTest(constraint = "Among", groups = {"vm2vm", "unit"})
    public void testAmongContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Among", groups = {"vm2vm", "unit"})
    public void testAmongContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "Among", groups = {"vm2vm", "unit"})
    public void testAmongDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Among", groups = {"vm2vm", "unit"})
    public void testAmongDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Ban
    @CstrTest(constraint = "Ban", groups = {"vm2pm", "unit"})
    public void testBanDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Ban", groups = {"vm2pm", "unit"})
    public void testBanDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Core
    @CstrTest(constraint = "noVMsOnOfflineNodes", groups = {"core", "unit"})
    public void testNoVMsOnOfflineNodes(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "toRunning", groups = {"core", "unit"})
    public void testToRunning(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "toSleeping", groups = {"core", "unit"})
    public void testToSleeping(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "toReady", groups = {"core", "unit"})
    public void testToReady(CTestCasesRunner r) {
        check(r.continuous());
    }

    //Fence
    @CstrTest(constraint = "Fence", groups = {"vm2pm", "unit"})
    public void testFenceDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Fence", groups = {"vm2pm", "unit"})
    public void testFenceDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    @CstrTest(constraint = "Gather", groups = {"vm2vm", "unit"})
    public void testGatherContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Gather", groups = {"vm2vm", "unit"})
    public void testGatherContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "Gather", groups = {"vm2vm", "unit"})
    public void testGatherDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Gather", groups = {"vm2vm", "unit"})
    public void testGatherDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Killed
    @CstrTest(constraint = "Killed", groups = {"states", "unit"})
    public void testKilledDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Killed", groups = {"states", "unit"})
    public void testKilledDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Lonely
    @CstrTest(constraint = "Lonely", groups = {"vm2vm", "unit"})
    public void testLonelyContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Lonely", groups = {"vm2vm", "unit"})
    public void testLonelyContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "Lonely", groups = {"vm2vm", "unit"})
    public void testLonelyDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Lonely", groups = {"vm2vm", "unit"})
    public void testLonelyDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    @CstrTest(constraint = "MaxOnline", groups = {"counting", "unit"})
    public void testMaxOnlineContinuous(CTestCasesRunner r) {
        check(r.continuous()).dom(new IntVerifDomain(0, 5));
    }

    //MaxOnline
    @CstrTest(constraint = "MaxOnline", groups = {"counting", "unit"})
    public void testMaxOnlineContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).dom(new IntVerifDomain(0, 5)).impl().repair(true);
    }

    @CstrTest(constraint = "MaxOnline", groups = {"counting", "unit"})
    public void testMaxOnlineDiscrete(CTestCasesRunner r) {
        check(r.discrete()).dom(new IntVerifDomain(0, 5));
    }

    @CstrTest(constraint = "MaxOnline", groups = {"counting", "unit"})
    public void testMaxOnlineDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).dom(new IntVerifDomain(0, 5)).impl().repair(true);
    }

    //Offline
    @CstrTest(constraint = "Offline", groups = {"states", "unit"})
    public void testOfflineDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Offline", groups = {"states", "unit"})
    public void testOfflineDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Online
    @CstrTest(constraint = "Online", groups = {"states", "unit"})
    public void testOnlineDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Online", groups = {"states", "unit"})
    public void testOnlineDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Quarantine
    @CstrTest(constraint = "Quarantine", groups = {"vm2vm", "unit"})
    public void testQuarantineContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Quarantine", groups = {"vm2vm", "unit"})
    public void testQuarantineContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    //Ready
    @CstrTest(constraint = "Ready", groups = {"states", "unit"})
    public void testReadyDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Ready", groups = {"states", "unit"})
    public void testReadyDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Root
    @CstrTest(constraint = "Root", groups = {"vm2vm", "unit"})
    public void testRootContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Root", groups = {"vm2vm", "unit"})
    public void testRootContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    //Running
    @CstrTest(constraint = "Running", groups = {"states", "unit"})
    public void testRunningDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Running", groups = {"states", "unit"})
    public void testRunningDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Sleeping
    @CstrTest(constraint = "Sleeping", groups = {"states", "unit"})
    public void testSleepingDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Sleeping", groups = {"states", "unit"})
    public void testSleepingDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Split
    @CstrTest(constraint = "Split", groups = {"vm2vm", "unit"})
    public void testSplitContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Split", groups = {"vm2vm", "unit"})
    public void testSplitContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "Split", groups = {"vm2vm", "unit"})
    public void testSplitDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Split", groups = {"vm2vm", "unit"})
    public void testSplitDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }


    //SplitAmong
    @CstrTest(constraint = "SplitAmong", groups = {"vm2vm", "unit"})
    public void testSplitAmongContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "SplitAmong", groups = {"vm2vm", "unit"})
    public void testSplitAmongContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "SplitAmong", groups = {"vm2vm", "unit"})
    public void testSplitAmongDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "SplitAmong", groups = {"vm2vm", "unit"})
    public void testSplitAmongDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }


    //Spread
    @CstrTest(constraint = "Spread", groups = {"vm2vm", "unit"})
    public void testSpreadContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Spread", groups = {"vm2vm", "unit"})
    public void testSpreadContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "Spread", groups = {"vm2vm", "unit"})
    public void testSpreadDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Spread", groups = {"vm2vm", "unit"})
    public void testSpreadDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    @CstrTest(constraint = "Seq", groups = {"vm2vm", "unit"})
    public void testSeqContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Seq", groups = {"vm2vm", "unit"})
    public void testSeqContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

}
