package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestQuarantine {

    @CstrTest(constraint = "quarantine", groups = {"vm2vm", "long"})
    public void testContinuous(CTestCasesRunner r) {
        TestUtils.longCheck(r.continuous());
    }

    @CstrTest(constraint = "quarantine", groups = {"vm2vm", "long"})
    public void testContinuousRepair(CTestCasesRunner r) {
        TestUtils.longCheck(r.continuous()).impl().repair(true);
    }

    /*@CstrTest(constraint = "quarantine", groups = {"vm2vm"})
    public void testDiscrete(CTestCasesRunner r) {
        r.discrete().timeout(5).maxTests(1000);
    }

    @CstrTest(constraint = "quarantine", groups = {"vm2vm"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        r.discrete().timeout(5).maxTests(1000).impl().repair(true);
    }                                                   */

}
