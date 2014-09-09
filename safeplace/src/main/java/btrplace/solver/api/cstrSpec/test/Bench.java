package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;
import btrplace.solver.api.cstrSpec.verification.spec.IntVerifDomain;

/**
 * @author Fabien Hermenier
 */
public class Bench {

    public static final CTestCasesRunner check(CTestCasesRunner r) {
        r.maxFailures(1);
        r.maxTests(10000);
        r.timeout(30);
        return r;
    }

    //Among
    @CstrTest(constraint = "among", groups = {"vm2vm", "unit"})
    public void testAmongContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "among", groups = {"vm2vm", "unit"})
    public void testAmongContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "among", groups = {"vm2vm", "unit"})
    public void testAmongDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "among", groups = {"vm2vm", "unit"})
    public void testAmongDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Ban
    @CstrTest(constraint = "ban", groups = {"vm2pm", "unit"})
    public void testBanDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "ban", groups = {"vm2pm", "unit"})
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
    @CstrTest(constraint = "fence", groups = {"vm2pm", "unit"})
    public void testFenceDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "fence", groups = {"vm2pm", "unit"})
    public void testFenceDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    @CstrTest(constraint = "gather", groups = {"vm2vm", "unit"})
    public void testGatherContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "gather", groups = {"vm2vm", "unit"})
    public void testGatherContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "gather", groups = {"vm2vm", "unit"})
    public void testGatherDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "gather", groups = {"vm2vm", "unit"})
    public void testGatherDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //killed
    @CstrTest(constraint = "killed", groups = {"states", "unit"})
    public void testKilledDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "killed", groups = {"states", "unit"})
    public void testKilledDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //lonely
    @CstrTest(constraint = "lonely", groups = {"vm2vm", "unit"})
    public void testLonelyContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "lonely", groups = {"vm2vm", "unit"})
    public void testLonelyContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "lonely", groups = {"vm2vm", "unit"})
    public void testLonelyDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "lonely", groups = {"vm2vm", "unit"})
    public void testLonelyDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    @CstrTest(constraint = "maxOnline", groups = {"counting", "unit"})
    public void testMaxOnlineContinuous(CTestCasesRunner r) {
        check(r.continuous()).dom(new IntVerifDomain(0, 5));
    }

    //maxOnline
    @CstrTest(constraint = "maxOnline", groups = {"counting", "unit"})
    public void testMaxOnlineContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).dom(new IntVerifDomain(0, 5)).impl().repair(true);
    }

    @CstrTest(constraint = "maxOnline", groups = {"counting", "unit"})
    public void testMaxOnlineDiscrete(CTestCasesRunner r) {
        check(r.discrete()).dom(new IntVerifDomain(0, 5));
    }

    @CstrTest(constraint = "maxOnline", groups = {"counting", "unit"})
    public void testMaxOnlineDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).dom(new IntVerifDomain(0, 5)).impl().repair(true);
    }

    //Offline
    @CstrTest(constraint = "offline", groups = {"states", "unit"})
    public void testOfflineDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "offline", groups = {"states", "unit"})
    public void testOfflineDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Online
    @CstrTest(constraint = "online", groups = {"states", "unit"})
    public void testOnlineDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "online", groups = {"states", "unit"})
    public void testOnlineDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Quarantine
    @CstrTest(constraint = "quarantine", groups = {"vm2vm", "unit"})
    public void testQuarantineContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "quarantine", groups = {"vm2vm", "unit"})
    public void testQuarantineContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    //Ready
    @CstrTest(constraint = "ready", groups = {"states", "unit"})
    public void testReadyDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "ready", groups = {"states", "unit"})
    public void testReadyDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Root
    @CstrTest(constraint = "root", groups = {"vm2vm", "unit"})
    public void testRootContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "root", groups = {"vm2vm", "unit"})
    public void testRootContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    //Running
    @CstrTest(constraint = "running", groups = {"states", "unit"})
    public void testRunningDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "running", groups = {"states", "unit"})
    public void testRunningDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Sleeping
    @CstrTest(constraint = "sleeping", groups = {"states", "unit"})
    public void testSleepingDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "sleeping", groups = {"states", "unit"})
    public void testSleepingDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    //Split
    @CstrTest(constraint = "split", groups = {"vm2vm", "unit"})
    public void testSplitContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "split", groups = {"vm2vm", "unit"})
    public void testSplitContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "split", groups = {"vm2vm", "unit"})
    public void testSplitDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "split", groups = {"vm2vm", "unit"})
    public void testSplitDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }


    //SplitAmong
    @CstrTest(constraint = "splitAmong", groups = {"vm2vm", "unit"})
    public void testSplitAmongContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "splitAmong", groups = {"vm2vm", "unit"})
    public void testSplitAmongContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "splitAmong", groups = {"vm2vm", "unit"})
    public void testSplitAmongDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "splitAmong", groups = {"vm2vm", "unit"})
    public void testSplitAmongDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }


    //Spread
    @CstrTest(constraint = "spread", groups = {"vm2vm", "unit"})
    public void testSpreadContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "spread", groups = {"vm2vm", "unit"})
    public void testSpreadContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "spread", groups = {"vm2vm", "unit"})
    public void testSpreadDiscrete(CTestCasesRunner r) {
        check(r.discrete());
    }

    @CstrTest(constraint = "spread", groups = {"vm2vm", "unit"})
    public void testSpreadDiscreteRepair(CTestCasesRunner r) {
        check(r.discrete()).impl().repair(true);
    }

    @CstrTest(constraint = "sequentialVMTransitions", groups = {"vm2vm", "unit"})
    public void testSeqContinuous(CTestCasesRunner r) {
        check(r.continuous());
    }

    @CstrTest(constraint = "sequentialVMTransitions", groups = {"vm2vm", "unit"})
    public void testSeqContinuousRepair(CTestCasesRunner r) {
        check(r.continuous()).impl().repair(true);
    }

}
