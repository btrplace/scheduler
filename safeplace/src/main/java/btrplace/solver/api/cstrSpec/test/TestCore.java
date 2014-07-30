package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestCore {

    @CstrTest(constraint = "noVMsOnOfflineNodes", groups = {"core"})
    public void testNoVMsOnOfflineNodes(CTestCasesRunner r) {
        r.continuous().timeout(5).maxTests(1000);
    }

    @CstrTest(constraint = "toRunning", groups = {"core"})
    public void testToRunning(CTestCasesRunner r) {
        r.continuous().timeout(5).maxTests(1000);
    }

    @CstrTest(constraint = "toSleeping", groups = {"core"})
    public void testToSleeping(CTestCasesRunner r) {
        r.continuous().timeout(5).maxTests(1000);
    }

    @CstrTest(constraint = "toReady", groups = {"core"})
    public void testToReady(CTestCasesRunner r) {
        r.continuous().timeout(5).maxTests(1000);
    }

}
