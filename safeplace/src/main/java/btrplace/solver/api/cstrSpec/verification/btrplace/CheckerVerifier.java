package btrplace.solver.api.cstrSpec.verification.btrplace;

import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanChecker;
import btrplace.plan.ReconfigurationPlanCheckerException;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;
import btrplace.solver.api.cstrSpec.verification.Verifier;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class CheckerVerifier implements Verifier {

    @Override
    public CheckerResult verify(Constraint cstr, Model src, Model res, List<Constant> params) {
        if (cstr.isCore()) {
            if (res == null) {
                return new CheckerResult(false, "Core constraint violation");
            }
            return CheckerResult.newSuccess();
        }
        try {
            SatConstraint sat = Constraint2BtrPlace.build(cstr, params);
            if (sat.setContinuous(false)) {

                if (res == null) {
                    //Core constraint violation
                    return new CheckerResult(false, "Core constraint violation");
                }
                if (!sat.getChecker().endsWith(res)) {
                    return new CheckerResult(false, "Violation of " + sat.toString());
                }
                return CheckerResult.newSuccess();
            } else {
                throw new UnsupportedOperationException(sat + " cannot be discrete");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public CheckerResult verify(Constraint cstr, ReconfigurationPlan p, List<Constant> params) {
        if (cstr.isCore()) {
            Model res = p.getResult();
            if (res == null) {
                return new CheckerResult(false, "Core constraint violation");
            }
            return CheckerResult.newSuccess();
        }
        try {
            SatConstraint sat = Constraint2BtrPlace.build(cstr, params);
            if (sat.setContinuous(true)) {
                ReconfigurationPlanChecker chk = new ReconfigurationPlanChecker();
                chk.addChecker(sat.getChecker());
                try {
                    chk.check(p);
                    return CheckerResult.newSuccess();
                } catch (ReconfigurationPlanCheckerException ex) {
                    //     ex.printStackTrace();
                    if (ex.getAction() == null) {
                        return CheckerResult.newFailure(ex.getMessage());
                    }
                    return CheckerResult.newFailure(ex.getAction());
                }
            } else {
                throw new UnsupportedOperationException(sat + " cannot be continuous");
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString() {
        return "checker";
    }
}
