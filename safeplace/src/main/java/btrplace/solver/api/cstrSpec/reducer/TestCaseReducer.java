package btrplace.solver.api.cstrSpec.reducer;

import btrplace.solver.api.cstrSpec.verification.TestCase;

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
        sr = new SignatureReducer();
        er = new ElementsReducer();
    }

    TestCase reduce(TestCase tc) throws Exception {
        //TestCase t = pr.reduce(tc);
        //List<Constant> reducedParams = sr.reduce(.getPlan(), tc.getConstraint(), tc.getArguments());
        //return er.reduce(t);
        //return new TestCase(tc.getVerifier(), tc.getConstraint(), reducedElements, reducedParams, tc.isDiscrete());
        return null;
    }
}
