package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;
import btrplace.solver.api.cstrSpec.verification.spec.IntVerifDomain;

/**
 * @author Fabien Hermenier
 */
public class TestMaxOnline {

    @CstrTest(constraint = "maxOnline", groups = {"counting", "unit"})
    public void testContinuous(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous()).dom(new IntVerifDomain(0, 5));
    }

    @CstrTest(constraint = "maxOnline", groups = {"counting", "unit"})
    public void testContinuousRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(r.continuous()).dom(new IntVerifDomain(0, 5)).impl().repair(true);
    }

    @CstrTest(constraint = "maxOnline", groups = {"counting", "unit"})
    public void testDiscrete(CTestCasesRunner r) {
        TestUtils.quickCheck(r.discrete()).dom(new IntVerifDomain(0, 5));
    }

    @CstrTest(constraint = "maxOnline", groups = {"counting", "unit"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(r.discrete()).dom(new IntVerifDomain(0, 5)).impl().repair(true);
        ;
    }
}
