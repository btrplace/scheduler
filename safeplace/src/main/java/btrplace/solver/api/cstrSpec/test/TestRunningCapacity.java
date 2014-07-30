package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;
import btrplace.solver.api.cstrSpec.verification.spec.IntVerifDomain;

/**
 * @author Fabien Hermenier
 */
public class TestRunningCapacity {

    @CstrTest(constraint = "runningCapacity", groups = {"counting", "long"})
    public void testContinuous(CTestCasesRunner r) {
        TestUtils.longCheck(r.continuous()).dom(new IntVerifDomain(0, 5));
    }

    @CstrTest(constraint = "runningCapacity", groups = {"counting", "long"})
    public void testContinuousRepair(CTestCasesRunner r) {
        TestUtils.longCheck(r.continuous()).dom(new IntVerifDomain(0, 5)).impl().repair(true);
    }

    @CstrTest(constraint = "runningCapacity", groups = {"counting", "long"})
    public void testDiscrete(CTestCasesRunner r) {
        TestUtils.longCheck(r.discrete()).dom(new IntVerifDomain(0, 5));
    }

    @CstrTest(constraint = "runningCapacity", groups = {"counting", "long"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        TestUtils.longCheck(r.discrete()).dom(new IntVerifDomain(0, 5)).impl().repair(true);
    }

}
