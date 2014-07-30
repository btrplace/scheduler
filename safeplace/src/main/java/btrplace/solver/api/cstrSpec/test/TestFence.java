package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestFence {

    @CstrTest(constraint = "fence", groups = {"vm2pm", "long"})
    public void testDiscrete(CTestCasesRunner r) {
        TestUtils.longCheck(r.discrete());
    }

    @CstrTest(constraint = "fence", groups = {"vm2pm", "long"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        TestUtils.longCheck(r.discrete()).impl().repair(true);
    }
}
