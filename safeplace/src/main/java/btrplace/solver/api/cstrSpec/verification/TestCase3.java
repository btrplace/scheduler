package btrplace.solver.api.cstrSpec.verification;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TestCase3 {

    private Constraint c;

    private ReconfigurationPlan plan;

    private List<Constant> args;

    private CheckerResult bImpl, bCheck, bSpec;

    private boolean d;

    public TestCase3(Constraint c, ReconfigurationPlan p, List<Constant> args, boolean d) {
        this.c = c;
        this.plan = p;
        this.args = args;
        this.d = d;
        bImpl = new ImplVerifier2().verify(c, plan, args, d);
        bCheck = new CheckerVerifier().verify(c, plan, args, d);
        bSpec = new SpecVerifier().verify(c, plan, args, d);
    }

    public boolean succeed() {
        return bImpl != null && bImpl.getStatus() == bCheck.getStatus() == bSpec.getStatus();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Mapping:\n").append(plan.getOrigin().getMapping());
        b.append("Constraint: ").append(c.toString(args)).append(" discrete: ").append(d).append("\n");
        b.append("Spec: ").append(bSpec);
        b.append("Checker: ").append(bCheck);
        b.append("Impl: ").append(bImpl);
        return b.toString();
    }
}
