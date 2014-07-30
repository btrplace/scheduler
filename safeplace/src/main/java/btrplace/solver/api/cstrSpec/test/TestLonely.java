package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestLonely {

    @CstrTest(constraint = "split", groups = {"vm2vm"})
    public void testContinuous(CTestCasesRunner r) {
        r.continuous().timeout(5).maxTests(1000);
    }

    @CstrTest(constraint = "split", groups = {"vm2vm"})
    public void testContinuousRepair(CTestCasesRunner r) {
        r.continuous().timeout(5).maxTests(1000).impl().repair(true);
    }

    @CstrTest(constraint = "split", groups = {"vm2vm"})
    public void testDiscrete(CTestCasesRunner r) {
        r.discrete().timeout(5).maxTests(1000);
    }

    @CstrTest(constraint = "split", groups = {"vm2vm"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        r.discrete().timeout(5).maxTests(1000).impl().repair(true);
    }

}
