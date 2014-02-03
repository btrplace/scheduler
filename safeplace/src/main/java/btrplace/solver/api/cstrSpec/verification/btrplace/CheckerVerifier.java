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
            return CheckerResult.newError(new IllegalArgumentException(cstr.id() + " is a core constraint. Not instantiable"));
        }
        try {
            SatConstraint sat = Constraint2BtrPlace.build(cstr, params);
            if (discrete) {
                if (sat.setContinuous(false)) {
                    Model res = p.getResult();
                    if (res == null) {
                        //Core constraint violation
                        return new CheckerResult(false, new ReconfigurationPlanCheckerException(null, res, true));
                    }
                    if (!sat.getChecker().endsWith(res)) {
                        return new CheckerResult(false, new ReconfigurationPlanCheckerException(sat, res, true));
                    }
                    return CheckerResult.newSucess();
                } else {
                    return new CheckerResult(false, new UnsupportedOperationException(sat + " cannot be discrete"));
                }
            } else {
                if (sat.setContinuous(true)) {
                    ReconfigurationPlanChecker chk = new ReconfigurationPlanChecker();
                    chk.addChecker(sat.getChecker());
                    try {
                        chk.check(p);
                        return CheckerResult.newSucess();
                    } catch (ReconfigurationPlanCheckerException ex) {
                        return CheckerResult.newFailure(ex);
                    }
                } else {
                    return new CheckerResult(false, new UnsupportedOperationException(sat + " cannot be continuous"));
                }
            }
        } catch (Exception ex) {
            return CheckerResult.newError(ex);
        }
    }
}
