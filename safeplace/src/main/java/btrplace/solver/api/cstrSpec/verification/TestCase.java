package btrplace.solver.api.cstrSpec.verification;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TestCase {

    private Constraint c;

    private ReconfigurationPlan plan;

    private List<Constant> args;

    private CheckerResult[] res;

    private Verifier verifier;

    private boolean d;

    public TestCase(Verifier v, Constraint c, ReconfigurationPlan p, List<Constant> args, boolean d) {
        res = new CheckerResult[2];
        res[0] = new SpecVerifier().verify(c, p, args, d);
        res[1] = v.verify(c, p, args, d);
        verifier = v;
        this.args = args;
        this.c = c;
        this.plan = p;
        this.d = d;
    }

    public boolean succeed() {
        return res[0].getStatus() == res[1].getStatus();
    }

    public Verifier getVerifier() {
        return verifier;
    }

    public Constraint getConstraint() {
        return c;
    }

    public ReconfigurationPlan getPlan() {
        return plan;
    }

    public List<Constant> getArguments() {
        return args;
    }

    public boolean isDiscrete() {
        return d;
    }

    public String pretty(boolean verbose) {
        StringBuilder b = new StringBuilder();
        if (d) {
            b.append("discrete ");
        } else {
            b.append("continuous ");
        }
        b.append(c.toString(args)).append(" ");
        b.append(succeed()).append("\n");
        b.append("spec: ").append(res[0]).append("\n");
        b.append(verifier.toString()).append(": ").append(res[1]);

        if (verbose) {
            b.append("\nSource Model:\n").append(plan.getOrigin().getMapping());
            b.append("Plan:\n").append(plan);
        }
        return b.toString();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (d) {
            b.append("discrete ");
        }
        b.append(c.toString(args));
        b.append(' ');
        b.append(succeed());
        return b.toString();
    }
}
