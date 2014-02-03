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
    public CheckerResult verify(Constraint cstr, ReconfigurationPlan p, List<Constant> params, boolean discrete) {
        if (cstr.isCore()) {
            //    return CheckerResult.newError(cstr.id() + " is a core constraint. Not instantiable");
            Model res = p.getResult();
            if (res == null) {
                return new CheckerResult(false, "Core constraint violation");
            }
            return CheckerResult.newSucess();
        }
        try {
            SatConstraint sat = Constraint2BtrPlace.build(cstr, params);
            if (discrete) {
                if (sat.setContinuous(false)) {
                    Model res = p.getResult();
                    if (res == null) {
                        //Core constraint violation
                        return new CheckerResult(false, "Core constraint violation");
                    }
                    if (!sat.getChecker().endsWith(res)) {
                        return new CheckerResult(false, "Violation of " + sat.toString());
                    }
                    return CheckerResult.newSucess();
                } else {
                    return new CheckerResult(false, sat + " cannot be discrete");
                }
            } else {
                if (sat.setContinuous(true)) {
                    ReconfigurationPlanChecker chk = new ReconfigurationPlanChecker();
                    chk.addChecker(sat.getChecker());
                    try {
                        chk.check(p);
                        return CheckerResult.newSucess();
                    } catch (ReconfigurationPlanCheckerException ex) {
                        return CheckerResult.newFailure(ex.toString());
                    }
                } else {
                    return new CheckerResult(false, sat + " cannot be continuous");
                }
            }
        } catch (Exception ex) {
            return CheckerResult.newError(ex.getMessage());
        }
    }
}
