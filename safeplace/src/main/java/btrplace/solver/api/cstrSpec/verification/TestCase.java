package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;

import java.util.List;
import java.util.Objects;

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

    public TestCase(CheckerResult specRes,
                    CheckerResult againstRes,
                    Verifier v,
                    Constraint c,
                    ReconfigurationPlan p,
                    List<Constant> args,
                    boolean d) {
        expected = specRes;
        got = againstRes;
        verifier = v;
        this.args = args;
        this.c = c;
        this.plan = p;
        this.d = d;
    }

    public TestCase(Verifier v, Constraint c, ReconfigurationPlan p, List<Constant> args, boolean d) {
        if (d) {
            Model src = p.getOrigin();
            Model dst = p.getResult();
            expected = new SpecVerifier().verify(c, src, dst, args);
            got = v.verify(c, src, dst, args);
        } else {
            expected = new SpecVerifier().verify(c, p, args);
            got = v.verify(c, p, args);
        }

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
            if (plan != null) {
                b.append("Plan:\n").append(plan);
            } else {
                Model dst = plan.getResult();
                if (dst == null) {
                    b.append("Resulting model: -\n");
                } else {
                    b.append("Resulting model:\n").append(dst.getMapping());
                }

            }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestCase testCase = (TestCase) o;

        if (d != testCase.d) return false;
        if (!args.equals(testCase.args)) return false;
        if (!c.equals(testCase.c)) return false;
        if (!expected.equals(testCase.expected)) return false;
        if (!got.equals(testCase.got)) return false;
        if (plan != null ? !plan.equals(testCase.plan) : testCase.plan != null) return false;
        if (!verifier.toString().equals(testCase.verifier.toString())) return false;

        return true;
    }

    public boolean falsePositive() {
        return !expected.getStatus() && got.getStatus();
    }

    public boolean falseNegative() {
        return expected.getStatus() && !got.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(c, plan, args, expected, got, verifier, d);
    }
}
