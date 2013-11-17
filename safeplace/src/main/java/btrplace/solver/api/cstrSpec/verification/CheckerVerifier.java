package btrplace.solver.api.cstrSpec.verification;

import btrplace.plan.ReconfigurationPlanChecker;

/**
 * @author Fabien Hermenier
 */
public class CheckerVerifier implements Verifier {

    @Override
    public TestResult verify(TestCase c) {
        ReconfigurationPlanChecker chk = new ReconfigurationPlanChecker();
        chk.addChecker(c.getSatConstraint().getChecker());
        TestResult.ErrorType err = TestResult.ErrorType.bug;

        try {
            chk.check(c.getPlan());
        } catch (Exception ex) {
            return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), err, ex);
        }
        err = c.isConsistent() ? TestResult.ErrorType.succeed : TestResult.ErrorType.falsePositive;

        return new TestResult(c.num(), c.getPlan(), c.getSatConstraint(), c.isConsistent(), err);
    }
}
