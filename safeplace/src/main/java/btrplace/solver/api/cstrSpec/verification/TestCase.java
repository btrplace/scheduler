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

    private CheckerResult expected, got;

    private Verifier verifier;

    private boolean d;

    public TestCase(CheckerResult specRes, CheckerResult againstRes, Verifier v, Constraint c, ReconfigurationPlan p, List<Constant> args, boolean d) {
        expected = specRes;
        got = againstRes;
        verifier = v;
        this.args = args;
        this.c = c;
        this.plan = p;
        this.d = d;
    }


    public TestCase(Verifier v, Constraint c, ReconfigurationPlan p, List<Constant> args, boolean d) {
        expected = new SpecVerifier().verify(c, p, args);
        got = v.verify(c, p, args);
        verifier = v;
        this.args = args;
        this.c = c;
        this.plan = p;
        this.d = d;
    }

    public boolean succeed() {
        return expected.getStatus() == got.getStatus();
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
        b.append("spec: ").append(expected).append("\n");
        b.append(verifier.toString()).append(": ").append(got);

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
