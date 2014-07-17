package btrplace.solver.api.cstrSpec.reducer;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;

import java.util.List;

/**
 * Reduce a test case as much as possible.
 * <p/>
 * This is done as follow:
 * <ol>
 * <li>Un-necessary actions are removed</li>
 * <li>Un-necessary values in the constraints signature are removed</li>
 * <li>Un-necessary elements in the plan are removed</li>
 * </ol>
 *
 * @author Fabien Hermenier
 */
public class TestCaseReducer {

    private PlanReducer pr;

    private SignatureReducer sr;

    private ElementsReducer er;

    public TestCaseReducer() {
        pr = new PlanReducer();
        sr = new SignatureReducer(null);
        er = new ElementsReducer(null);
    }

    TestCase reduce(TestCase tc) throws Exception {
        TestCase reducedPlan = pr.reduce(tc);
        List<Constant> reducedParams = sr.reduce(reducedPlan.getPlan(), tc.getConstraint(), tc.getArguments());
        ReconfigurationPlan reducedElements = er.reduce(reducedPlan.getPlan(), tc.getConstraint(), reducedParams);
        return new TestCase(tc.getVerifier(), tc.getConstraint(), reducedElements, reducedParams, tc.isDiscrete());
    }
}
