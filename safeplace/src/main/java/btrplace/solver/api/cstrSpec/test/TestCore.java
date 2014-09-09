package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestCore {

    @CstrTest(constraint = "noVMsOnOfflineNodes", groups = {"core", "unit"})
    public void testNoVMsOnOfflineNodes(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous());
    }

    @CstrTest(constraint = "toRunning", groups = {"core", "unit"})
    public void testToRunning(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous());
    }

    @CstrTest(constraint = "toSleeping", groups = {"core", "unit"})
    public void testToSleeping(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous());
    }

    @CstrTest(constraint = "toReady", groups = {"core", "unit"})
    public void testToReady(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous());
    }

}
