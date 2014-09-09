package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestLonely {

    @CstrTest(constraint = "split", groups = {"vm2vm", "unit"})
    public void testContinuous(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous());
    }

    @CstrTest(constraint = "split", groups = {"vm2vm", "unit"})
    public void testContinuousRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "split", groups = {"vm2vm", "unit"})
    public void testDiscrete(CTestCasesRunner r) {
        TestUtils.quickCheck(r.discrete());
    }

    @CstrTest(constraint = "split", groups = {"vm2vm", "unit"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(r.discrete()).impl().repair(true);
    }

}
