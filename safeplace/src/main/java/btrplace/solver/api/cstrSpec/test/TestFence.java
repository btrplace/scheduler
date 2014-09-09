package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestFence {

    @CstrTest(constraint = "fence", groups = {"vm2pm", "unit"})
    public void testDiscrete(CTestCasesRunner r) {
        TestUtils.quickCheck(r.discrete());
    }

    @CstrTest(constraint = "fence", groups = {"vm2pm", "unit"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(r.discrete()).impl().repair(true);
    }
}
