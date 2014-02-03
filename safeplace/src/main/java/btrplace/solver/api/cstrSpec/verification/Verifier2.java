package btrplace.solver.api.cstrSpec.verification;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public interface Verifier2 {

    CheckerResult verify(Constraint c, ReconfigurationPlan p, List<Constant> params, boolean discrete);
}
