package btrplace.solver.api.cstrSpec.reducer;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.CTestCase;
import btrplace.solver.api.cstrSpec.CTestCaseResult;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public abstract class Reducer {
    public abstract CTestCase reduce(CTestCase tc, SpecVerifier v1, Verifier v2, CTestCaseResult.Result errType) throws Exception;

    public CTestCase derive(CTestCase tc, List<Constant> args, ReconfigurationPlan p) {
        return new CTestCase(tc.getTestClass(), tc.getTestName(), tc.getNumber(), tc.getConstraint(), args, p, tc.continuous());
    }

    public boolean consistent(SpecVerifier v1, Verifier v2, Constraint cstr, List<Constant> args, ReconfigurationPlan p, boolean c, CTestCaseResult.Result errType) {
        boolean r1, r2;
        if (c) {
            r1 = v1.verify(cstr, args, p).getStatus();
            r2 = v2.verify(cstr, args, p).getStatus();
        } else {
            Model src = p.getOrigin();
            Model dst = p.getResult();
            r1 = v1.verify(cstr, args, src, dst).getStatus();
            r2 = v2.verify(cstr, args, src, dst).getStatus();
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

    public boolean consistent(SpecVerifier v1, Verifier v2, CTestCase tc, CTestCaseResult.Result errType) {
        return consistent(v1, v2, tc.getConstraint(), tc.getParameters(), tc.getPlan(), tc.continuous(), errType);
    }

}
