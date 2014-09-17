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
import btrplace.safeplace.annotations.CstrTestsProvider;
import btrplace.safeplace.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.safeplace.runner.CTestCasesRunner;
import btrplace.safeplace.verification.spec.IntVerifDomain;

/**
 * @author Fabien Hermenier
 */
public class Bench {

    public static int nbVMs = 3;
    public static int nbNodes = 3;

    public static boolean reduce = true;
    public static int tests = 100;
    public static int to = 10000;

    public static boolean validate = true;

    public static boolean checkers = false;

    public static final CTestCasesRunner check(CTestCasesRunner r) {
        r.maxTests(tests);
        r.timeout(to * 1000);
        r.validate(validate);
        if (checkers) {
            r.checker();
        } else {
            r.impl();
        }
        if (!reduce) {
            r.clearReducers();
        }
        return r;
    }

    public static CTestCasesRunner repair(CTestCasesRunner r) {
        if (!checkers) {
            r.impl().repair(true);
        }
        return r;
    }

    @CstrTestsProvider(name = "my")
    public ReconfigurationPlanFuzzer2 myProvider() {
        return new ReconfigurationPlanFuzzer2().nbVMs(nbVMs).nbNodes(nbNodes);
    }

    //Among
    @CstrTest(constraint = "Among", groups = {"vm2vm", "unit", "rebuild"}, provider = "myProvider")
    public void testAmongContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Among", groups = {"vm2vm", "unit", "repair"}, provider = "myProvider")
    public void testAmongContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Among", groups = {"vm2vm", "unit", "among", "rebuild"}, provider = "myProvider")
    public void testAmongDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Among", groups = {"vm2vm", "unit", "repair"}, provider = "myProvider")
    public void testAmongDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    //Ban
    @CstrTest(constraint = "Ban", groups = {"vm2pm", "unit", "ban", "rebuild"}, provider = "myProvider")
    public void testBanDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Ban", groups = {"vm2pm", "unit", "repair"}, provider = "myProvider")
    public void testBanDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Ban", groups = {"vm2pm", "unit", "ban", "rebuild"}, provider = "myProvider")
    public void testBanContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Ban", groups = {"vm2pm", "unit", "repair"}, provider = "myProvider")
    public void testBanContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    //Core
    @CstrTest(constraint = "noVMsOnOfflineNodes", groups = {"core", "unit", "rebuild"}, provider = "myProvider")
    public void testNoVMsOnOfflineNodes(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "toRunning", groups = {"core", "unit", "rebuild"}, provider = "myProvider")
    public void testToRunning(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "toSleeping", groups = {"core", "unit", "rebuild"}, provider = "myProvider")
    public void testToSleeping(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "toReady", groups = {"core", "unit", "rebuild"}, provider = "myProvider")
    public void testToReady(CTestCasesRunner r) {
        check(r.continuous());
    }

    //Fence
    @CstrTest(constraint = "Fence", groups = {"vm2pm", "unit", "fence", "rebuild"}, provider = "myProvider")
    public void testFenceDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Fence", groups = {"vm2pm", "unit", "fence", "repair"}, provider = "myProvider")
    public void testFenceDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Fence", groups = {"vm2pm", "unit", "fence", "rebuild"}, provider = "myProvider")
    public void testFenceContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Fence", groups = {"vm2pm", "unit", "fence", "repair"}, provider = "myProvider")
    public void testFenceContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }


    //Gather
    @CstrTest(constraint = "Gather", groups = {"vm2vm", "unit", "rebuild"}, provider = "myProvider")
    public void testGatherContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Gather", groups = {"vm2vm", "unit", "repair"}, provider = "myProvider")
    public void testGatherContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Gather", groups = {"vm2vm", "unit", "rebuild"}, provider = "myProvider")
    public void testGatherDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Gather", groups = {"vm2vm", "unit", "repair"}, provider = "myProvider")
    public void testGatherDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    //Killed
    @CstrTest(constraint = "Killed", groups = {"states", "unit", "rebuild"}, provider = "myProvider")
    public void testKilledDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Killed", groups = {"states", "unit", "repair"}, provider = "myProvider")
    public void testKilledDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Killed", groups = {"vm2pm", "unit", "ban", "rebuild"}, provider = "myProvider")
    public void testKilledContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Killed", groups = {"vm2pm", "unit", "repair"}, provider = "myProvider")
    public void testKilledContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }


    //Lonely
    @CstrTest(constraint = "Lonely", groups = {"vm2vm", "unit", "rebuild"}, provider = "myProvider")
    public void testLonelyContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Lonely", groups = {"vm2vm", "unit", "repair"}, provider = "myProvider")
    public void testLonelyContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Lonely", groups = {"vm2vm", "unit", "rebuild"}, provider = "myProvider")
    public void testLonelyDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Lonely", groups = {"vm2vm", "unit", "repair"}, provider = "myProvider")
    public void testLonelyDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    //MaxOnline
    @CstrTest(constraint = "MaxOnline", groups = {"counting", "unit", "rebuild"}, provider = "myProvider")
    public void testMaxOnlineContinuous(CTestCasesRunner r) {
        check(r.continuous()).dom(new IntVerifDomain(0, 5));
    }

    @CstrTest(constraint = "MaxOnline", groups = {"counting", "unit", "repair"}, provider = "myProvider")
    public void testMaxOnlineContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()).dom(new IntVerifDomain(0, 5)));//.impl().repair(true);
    }

    @CstrTest(constraint = "MaxOnline", groups = {"counting", "unit", "rebuild"}, provider = "myProvider")
    public void testMaxOnlineDiscrete(CTestCasesRunner r) {
        check(r.discrete()).dom(new IntVerifDomain(0, 5));
    }

    @CstrTest(constraint = "MaxOnline", groups = {"counting", "unit", "repair"}, provider = "myProvider")
    public void testMaxOnlineDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()).dom(new IntVerifDomain(0, 5)));//.impl().repair(true);
    }

    //Offline
    @CstrTest(constraint = "Offline", groups = {"states", "unit", "rebuild"}, provider = "myProvider")
    public void testOfflineDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Offline", groups = {"states", "unit", "repair"}, provider = "myProvider")
    public void testOfflineDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Offline", groups = {"vm2pm", "unit", "ban", "rebuild"}, provider = "myProvider")
    public void testOfflineContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Offline", groups = {"vm2pm", "unit", "repair"}, provider = "myProvider")
    public void testOfflineContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    //Online
    @CstrTest(constraint = "Online", groups = {"states", "unit", "rebuild"}, provider = "myProvider")
    public void testOnlineDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Online", groups = {"states", "unit", "repair"}, provider = "myProvider")
    public void testOnlineDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Online", groups = {"vm2pm", "unit", "ban", "rebuild"}, provider = "myProvider")
    public void testOnlineContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Online", groups = {"vm2pm", "unit", "repair"}, provider = "myProvider")
    public void testOnlineContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }


    //Quarantine
    @CstrTest(constraint = "Quarantine", groups = {"vm2vm", "unit", "rebuild"}, provider = "myProvider")
    public void testQuarantineContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Quarantine", groups = {"vm2vm", "unit", "repair"}, provider = "myProvider")
    public void testQuarantineContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    //Ready
    @CstrTest(constraint = "Ready", groups = {"states", "unit", "rebuild"}, provider = "myProvider")
    public void testReadyDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Ready", groups = {"states", "unit", "repair"}, provider = "myProvider")
    public void testReadyDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Ready", groups = {"vm2pm", "unit", "ban", "rebuild"}, provider = "myProvider")
    public void testReadyContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Ready", groups = {"vm2pm", "unit", "repair"}, provider = "myProvider")
    public void testReadyContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }


    //Root
    @CstrTest(constraint = "Root", groups = {"vm2vm", "unit", "rebuild"}, provider = "myProvider")
    public void testRootContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Root", groups = {"vm2vm", "unit", "repair"}, provider = "myProvider")
    public void testRootContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    //Running
    @CstrTest(constraint = "Running", groups = {"states", "unit", "rebuild"})
    public void testRunningDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Running", groups = {"states", "unit", "repair"}, provider = "myProvider")
    public void testRunningDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Running", groups = {"vm2pm", "unit", "ban", "rebuild"}, provider = "myProvider")
    public void testRunningContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Running", groups = {"vm2pm", "unit", "repair"}, provider = "myProvider")
    public void testRunningContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }


    //Sleeping
    @CstrTest(constraint = "Sleeping", groups = {"states", "unit", "rebuild"}, provider = "myProvider")
    public void testSleepingDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Sleeping", groups = {"states", "unit", "repair"}, provider = "myProvider")
    public void testSleepingDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Sleeping", groups = {"vm2pm", "unit", "ban", "rebuild"}, provider = "myProvider")
    public void testSleepingContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Sleeping", groups = {"vm2pm", "unit", "repair"}, provider = "myProvider")
    public void testSleepingContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }


    //Split
    @CstrTest(constraint = "Split", groups = {"vm2vm", "unit", "split", "rebuild"}, provider = "myProvider")
    public void testSplitContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Split", groups = {"vm2vm", "unit", "repair"}, provider = "myProvider")
    public void testSplitContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Split", groups = {"vm2vm", "unit", "rebuild"}, provider = "myProvider")
    public void testSplitDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Split", groups = {"vm2vm", "unit", "repair"}, provider = "myProvider")
    public void testSplitDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }


    //SplitAmong
    @CstrTest(constraint = "SplitAmong", groups = {"vm2vm", "unit", "rebuild"}, provider = "myProvider")
    public void testSplitAmongContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "SplitAmong", groups = {"vm2vm", "unit", "repair"}, provider = "myProvider")
    public void testSplitAmongContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    @CstrTest(constraint = "SplitAmong", groups = {"vm2vm", "unit", "rebuild"}, provider = "myProvider")
    public void testSplitAmongDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "SplitAmong", groups = {"vm2vm", "unit", "repair"}, provider = "myProvider")
    public void testSplitAmongDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }


    //Spread
    @CstrTest(constraint = "Spread", groups = {"vm2vm", "unit", "rebuild"}, provider = "myProvider")
    public void testSpreadContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Spread", groups = {"vm2vm", "unit", "repair"}, provider = "myProvider")
    public void testSpreadContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Spread", groups = {"vm2vm", "unit", "rebuild"}, provider = "myProvider")
    public void testSpreadDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Spread", groups = {"vm2vm", "unit", "repair"}, provider = "myProvider")
    public void testSpreadDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    /*@CstrTest(constraint = "Seq", groups = {"vm2vm", "unit"}, provider = "myProvider")
    public void testSeqContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Seq", groups = {"vm2vm", "unit","seq"}, provider = "myProvider")
    public void testSeqContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }*/
}
