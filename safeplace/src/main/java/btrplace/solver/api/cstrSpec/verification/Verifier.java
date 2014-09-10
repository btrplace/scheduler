package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public interface Verifier {

    CheckerResult verify(Constraint c, List<Constant> params, Model dst, Model src);

    CheckerResult verify(Constraint c, List<Constant> params, ReconfigurationPlan p);
}
