package btrplace.solver.api.cstrSpec.reducer;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.CTestCase;
import btrplace.solver.api.cstrSpec.CTestCaseResult;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;

/**
 * @author Fabien Hermenier
 */
public abstract class Reducer {
    public abstract CTestCase reduce(CTestCase tc, SpecVerifier v1, Verifier v2, CTestCaseResult.Result errType) throws Exception;

    public boolean consistent(SpecVerifier v1, Verifier v2, CTestCase tc, CTestCaseResult.Result errType) {
        boolean r1, r2;
        if (tc.continuous()) {
            r1 = v1.verify(tc.getConstraint(), tc.getParameters(), tc.getPlan()).getStatus();
            r2 = v2.verify(tc.getConstraint(), tc.getParameters(), tc.getPlan()).getStatus();
        } else {
            Model src = tc.getPlan().getOrigin();
            Model dst = tc.getPlan().getResult();
            r1 = v1.verify(tc.getConstraint(), tc.getParameters(), src, dst).getStatus();
            r2 = v2.verify(tc.getConstraint(), tc.getParameters(), src, dst).getStatus();
        }
        //System.out.println("With " + tc.getConstraint().toString(tc.getParameters()) + " " + r1 + " " + r2);
        if (r1 == r2) {
            return true;
        }
        //System.out.println(errType + " to " + r1 + " " + r2);
        //We maintain the error type
        if ((errType == CTestCaseResult.Result.falseNegative && !r2) || (errType == CTestCaseResult.Result.falsePositive && r2)) {
            return false;
        }
        return true;
    }

}
