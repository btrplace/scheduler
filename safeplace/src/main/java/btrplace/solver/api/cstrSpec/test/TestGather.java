package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestGather {

    @CstrTest(constraint = "gather", groups = {"vm2vm", "long"})
    public void testContinuous(CTestCasesRunner r) {
        TestUtils.longCheck(r.continuous());
    }

    @CstrTest(constraint = "gather", groups = {"vm2vm", "long"})
    public void testContinuousRepair(CTestCasesRunner r) {
        TestUtils.longCheck(r.continuous()).impl().repair(true);
    }

    @CstrTest(constraint = "gather", groups = {"vm2vm", "long"})
    public void testDiscrete(CTestCasesRunner r) {
        TestUtils.longCheck(r.discrete());
    }

    @CstrTest(constraint = "gather", groups = {"vm2vm", "long"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        TestUtils.longCheck(r.discrete()).impl().repair(true);
    }

}
