package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;
import btrplace.solver.api.cstrSpec.verification.spec.IntVerifDomain;

/**
 * @author Fabien Hermenier
 */
public class TestMaxOnline {

    @CstrTest(constraint = "maxOnline", groups = {"counting"})
    public void testContinuous(CTestCasesRunner r) {
        r.continuous().timeout(5).maxTests(1000).dom(new IntVerifDomain(0, 5));
    }

    @CstrTest(constraint = "maxOnline", groups = {"counting"})
    public void testContinuousRepair(CTestCasesRunner r) {
        r.continuous().timeout(5).maxTests(1000).dom(new IntVerifDomain(0, 5)).impl().repair(true);
    }

    @CstrTest(constraint = "maxOnline", groups = {"counting"})
    public void testDiscrete(CTestCasesRunner r) {
        r.discrete().timeout(5).maxTests(1000).dom(new IntVerifDomain(0, 5));
    }

    @CstrTest(constraint = "maxOnline", groups = {"counting"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        r.discrete().timeout(5).maxTests(1000).dom(new IntVerifDomain(0, 5)).impl().repair(true);
        ;
    }
}
