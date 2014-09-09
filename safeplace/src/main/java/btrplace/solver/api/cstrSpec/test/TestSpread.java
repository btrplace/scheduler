package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestSpread {

    @CstrTest(constraint = "spread", groups = {"vm2vm", "unit"})
    public void testContinuous(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous());
    }

    @CstrTest(constraint = "spread", groups = {"vm2vm", "unit"})
    public void testContinuousRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "spread", groups = {"vm2vm", "unit"})
    public void testDiscrete(CTestCasesRunner r) {
        TestUtils.quickCheck(r.discrete());
    }

    @CstrTest(constraint = "spread", groups = {"vm2vm", "unit"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(r.discrete()).impl().repair(true);
    }

}
