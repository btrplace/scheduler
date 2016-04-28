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

import org.btrplace.safeplace.fuzzer.Fuzzer;
import org.btrplace.safeplace.fuzzer.FuzzerImpl;
import org.btrplace.safeplace.runner.TestCasesRunner;
import org.btrplace.safeplace.runner.limit.MaxTests;
import org.btrplace.safeplace.scanner.CstrTest;
import org.btrplace.safeplace.scanner.CstrTestsProvider;
import org.btrplace.safeplace.verification.btrplace.ImplVerifier;

/**
 * @author Fabien Hermenier
 */
public class Bench {

    public static final TestCasesRunner check(TestCasesRunner r) {
        r.limit(new MaxTests(1000));
        r.timeout(5);
        r.verifier(new ImplVerifier());
        return r;
    }

    @CstrTestsProvider(name = "myProvider")
    public Fuzzer myProvider() {
        return new FuzzerImpl().nodes(1).vms(1).actionBounds(1, 3);
    }

    @CstrTest(constraint = "noVMsOnOfflineNodes", groups = {"core", "unit", "rebuild", "noVMsOnOfflineNodes"}, provider = "myProvider")
    public void testNoVMsOnOfflineNodes(TestCasesRunner r) {
        check(r);
    }

    @CstrTest(constraint = "toRunning", groups = {"core", "unit", "rebuild", "toRunning"}, provider = "myProvider")
    public void testToRunning(TestCasesRunner r) {
        check(r);
    }

    @CstrTest(constraint = "toSleeping", groups = {"core", "unit", "rebuild", "toSleeping"}, provider = "myProvider")
    public void testToSleeping(TestCasesRunner r) {
        check(r);
    }

    @CstrTest(constraint = "toReady", groups = {"core", "unit", "rebuild", "toReady"}, provider = "myProvider")
    public void testToReady(TestCasesRunner r) {
        check(r);
    }

    /*
    //Among
    @CstrTest(constraint = "Among", groups = {"vm2vm", "unit", "rebuild", "among"}, provider = "myProvider")
    public void testAmongContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Among", groups = {"vm2vm", "unit", "repair", "among"}, provider = "myProvider")
    public void testAmongContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Among", groups = {"vm2vm", "unit", "among", "rebuild", "among"}, provider = "myProvider")
    public void testAmongDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Among", groups = {"vm2vm", "unit", "repair", "among"}, provider = "myProvider")
    public void testAmongDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    //Ban
    @CstrTest(constraint = "Ban", groups = {"vm2pm", "unit", "ban", "rebuild"}, provider = "myProvider")
    public void testBanDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Ban", groups = {"vm2pm", "unit", "repair", "ban"}, provider = "myProvider")
    public void testBanDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Ban", groups = {"vm2pm", "unit", "ban", "rebuild"}, provider = "myProvider")
    public void testBanContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Ban", groups = {"vm2pm", "unit", "repair", "ban"}, provider = "myProvider")
    public void testBanContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
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
    @CstrTest(constraint = "Gather", groups = {"vm2vm", "unit", "rebuild", "gather"}, provider = "myProvider")
    public void testGatherContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Gather", groups = {"vm2vm", "unit", "repair", "gather"}, provider = "myProvider")
    public void testGatherContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Gather", groups = {"vm2vm", "unit", "rebuild", "gather"}, provider = "myProvider")
    public void testGatherDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Gather", groups = {"vm2vm", "unit", "repair", "gather"}, provider = "myProvider")
    public void testGatherDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    //Killed
    @CstrTest(constraint = "Killed", groups = {"states", "unit", "rebuild", "killed"}, provider = "myProvider")
    public void testKilledDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Killed", groups = {"states", "unit", "repair", "killed"}, provider = "myProvider")
    public void testKilledDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Killed", groups = {"vm2pm", "unit", "ban", "rebuild", "killed"}, provider = "myProvider")
    public void testKilledContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Killed", groups = {"vm2pm", "unit", "repair", "killed"}, provider = "myProvider")
    public void testKilledContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }


    //Lonely
    @CstrTest(constraint = "Lonely", groups = {"vm2vm", "unit", "rebuild", "lonely"}, provider = "myProvider")
    public void testLonelyContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Lonely", groups = {"vm2vm", "unit", "repair", "lonely"}, provider = "myProvider")
    public void testLonelyContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Lonely", groups = {"vm2vm", "unit", "rebuild", "lonely"}, provider = "myProvider")
    public void testLonelyDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Lonely", groups = {"vm2vm", "unit", "repair", "lonely"}, provider = "myProvider")
    public void testLonelyDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    //MaxOnline
    @CstrTest(constraint = "MaxOnline", groups = {"counting", "unit", "rebuild", "maxOnline"}, provider = "myProvider")
    public void testMaxOnlineContinuous(CTestCasesRunner r) {
        check(r.continuous()).dom(new IntDomain(0, 5));
    }

    @CstrTest(constraint = "MaxOnline", groups = {"counting", "unit", "repair", "maxOnline"}, provider = "myProvider")
    public void testMaxOnlineContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()).dom(new IntDomain(0, 5)));//.impl().repair(true);
    }

    @CstrTest(constraint = "MaxOnline", groups = {"counting", "unit", "rebuild", "maxOnline"}, provider = "myProvider")
    public void testMaxOnlineDiscrete(CTestCasesRunner r) {
        check(r.discrete()).dom(new IntDomain(0, 5));
    }

    @CstrTest(constraint = "MaxOnline", groups = {"counting", "unit", "repair", "maxOnline"}, provider = "myProvider")
    public void testMaxOnlineDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()).dom(new IntDomain(0, 5)));//.impl().repair(true);
    }

    //Offline
    @CstrTest(constraint = "Offline", groups = {"states", "unit", "rebuild", "offline"}, provider = "myProvider")
    public void testOfflineDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Offline", groups = {"states", "unit", "repair", "offline"}, provider = "myProvider")
    public void testOfflineDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Offline", groups = {"vm2pm", "unit", "ban", "rebuild", "offline"}, provider = "myProvider")
    public void testOfflineContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Offline", groups = {"vm2pm", "unit", "repair", "offline"}, provider = "myProvider")
    public void testOfflineContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    //Online
    @CstrTest(constraint = "Online", groups = {"states", "unit", "rebuild", "online"}, provider = "myProvider")
    public void testOnlineDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Online", groups = {"states", "unit", "repair", "online"}, provider = "myProvider")
    public void testOnlineDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Online", groups = {"vm2pm", "unit", "ban", "rebuild", "online"}, provider = "myProvider")
    public void testOnlineContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Online", groups = {"vm2pm", "unit", "repair", "online"}, provider = "myProvider")
    public void testOnlineContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }


    //Quarantine
    @CstrTest(constraint = "Quarantine", groups = {"vm2vm", "unit", "rebuild", "quarantine"}, provider = "myProvider")
    public void testQuarantineContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Quarantine", groups = {"vm2vm", "unit", "repair", "quarantine"}, provider = "myProvider")
    public void testQuarantineContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    //Ready
    @CstrTest(constraint = "Ready", groups = {"states", "unit", "rebuild", "ready"}, provider = "myProvider")
    public void testReadyDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Ready", groups = {"states", "unit", "repair", "ready"}, provider = "myProvider")
    public void testReadyDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Ready", groups = {"vm2pm", "unit", "ban", "rebuild", "ready"}, provider = "myProvider")
    public void testReadyContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Ready", groups = {"vm2pm", "unit", "repair", "ready"}, provider = "myProvider")
    public void testReadyContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }


    //Root
    @CstrTest(constraint = "Root", groups = {"vm2vm", "unit", "rebuild", "root"}, provider = "myProvider")
    public void testRootContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Root", groups = {"vm2vm", "unit", "repair", "root"}, provider = "myProvider")
    public void testRootContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    //Running
    @CstrTest(constraint = "Running", groups = {"states", "unit", "rebuild", "running"})
    public void testRunningDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Running", groups = {"states", "unit", "repair", "running"}, provider = "myProvider")
    public void testRunningDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Running", groups = {"vm2pm", "unit", "running", "rebuild"}, provider = "myProvider")
    public void testRunningContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Running", groups = {"vm2pm", "unit", "repair", "running"}, provider = "myProvider")
    public void testRunningContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }


    //Sleeping
    @CstrTest(constraint = "Sleeping", groups = {"states", "unit", "rebuild", "sleeping"}, provider = "myProvider")
    public void testSleepingDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Sleeping", groups = {"states", "unit", "repair", "sleeping"}, provider = "myProvider")
    public void testSleepingDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Sleeping", groups = {"vm2pm", "unit", "rebuild", "sleeping"}, provider = "myProvider")
    public void testSleepingContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Sleeping", groups = {"vm2pm", "unit", "repair", "sleeping"}, provider = "myProvider")
    public void testSleepingContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }


    //Split
    @CstrTest(constraint = "Split", groups = {"vm2vm", "unit", "split", "rebuild"}, provider = "myProvider")
    public void testSplitContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Split", groups = {"vm2vm", "unit", "repair", "split"}, provider = "myProvider")
    public void testSplitContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Split", groups = {"vm2vm", "unit", "rebuild", "split"}, provider = "myProvider")
    public void testSplitDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Split", groups = {"vm2vm", "unit", "repair", "split"}, provider = "myProvider")
    public void testSplitDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }


    //SplitAmong
    @CstrTest(constraint = "SplitAmong", groups = {"vm2vm", "unit", "rebuild", "splitAmong"}, provider = "myProvider")
    public void testSplitAmongContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "SplitAmong", groups = {"vm2vm", "unit", "repair", "splitAmong"}, provider = "myProvider")
    public void testSplitAmongContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    @CstrTest(constraint = "SplitAmong", groups = {"vm2vm", "unit", "rebuild", "splitAmong"}, provider = "myProvider")
    public void testSplitAmongDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "SplitAmong", groups = {"vm2vm", "unit", "repair", "splitAmong"}, provider = "myProvider")
    public void testSplitAmongDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }


    //Spread
    @CstrTest(constraint = "Spread", groups = {"vm2vm", "unit", "rebuild", "spread"}, provider = "myProvider")
    public void testSpreadContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "Spread", groups = {"vm2vm", "unit", "repair", "spread"}, provider = "myProvider")
    public void testSpreadContinuousRepair(CTestCasesRunner r) {
        repair(check(r.continuous()));//.impl().repair(true);
    }

    @CstrTest(constraint = "Spread", groups = {"vm2vm", "unit", "rebuild", "spread"}, provider = "myProvider")
    public void testSpreadDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "Spread", groups = {"vm2vm", "unit", "repair", "spread"}, provider = "myProvider")
    public void testSpreadDiscreteRepair(CTestCasesRunner r) {
        repair(check(r.discrete()));//.impl().repair(true);
    }

    */
}