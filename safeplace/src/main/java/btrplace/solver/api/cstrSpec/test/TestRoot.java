package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestRoot {

    @CstrTest(constraint = "root", groups = {"vm2vm", "unit"})
    public void testContinuous(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous());
    }

    @CstrTest(constraint = "root", groups = {"vm2vm", "unit"})
    public void testContinuousRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous()).impl().repair(true);
    }

    /*
    @CstrTest(constraint = "root", groups = {"vm2vm"})
    public void testDiscrete(CTestCasesRunner r) {
        r.discrete().timeout(5).maxTests(1000);
    }

    @CstrTest(constraint = "root", groups = {"vm2vm"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        r.discrete().timeout(5).maxTests(1000).impl().repair(true);
    }

    */
}
