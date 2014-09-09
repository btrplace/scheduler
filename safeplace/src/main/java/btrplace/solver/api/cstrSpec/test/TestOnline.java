package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestOnline {

    /*@CstrTest(constraint = "online", groups = {"states"})
    public void testContinuous(CTestCasesRunner r) {
        r.continuous().timeout(5).maxTests(1000);
    }

    @CstrTest(constraint = "online", groups = {"states"})
    public void testContinuousRepair(CTestCasesRunner r) {
        r.continuous().timeout(5).maxTests(1000).impl().repair(true);
    }  */

    @CstrTest(constraint = "online", groups = {"states", "unit"})
    public void testDiscrete(CTestCasesRunner r) {
        TestUtils.quickCheck(r.discrete());
    }

    @CstrTest(constraint = "online", groups = {"states", "unit"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(r.discrete()).impl().repair(true);
    }

}
