package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestCore {

    @CstrTest(constraint = "noVMsOnOfflineNodes", groups = {"core", "long"})
    public void testNoVMsOnOfflineNodes(CTestCasesRunner r) {
        TestUtils.longCheck(r.continuous());
    }

    @CstrTest(constraint = "toRunning", groups = {"core", "long"})
    public void testToRunning(CTestCasesRunner r) {
        TestUtils.longCheck(r.continuous());
    }

    @CstrTest(constraint = "toSleeping", groups = {"core", "long"})
    public void testToSleeping(CTestCasesRunner r) {
        TestUtils.longCheck(r.continuous());
    }

    @CstrTest(constraint = "toReady", groups = {"core", "long"})
    public void testToReady(CTestCasesRunner r) {
        TestUtils.longCheck(r.continuous());
    }

}
