package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.Model;
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

    private Model src, dst;
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
        this.src = plan.getOrigin();
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
            b.append("\nSource Model:\n").append(src.getMapping());
            if (plan != null) {
                b.append("Plan:\n").append(plan);
            } else {
                b.append("Resulting model:\n").append(src.getMapping());
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
        if (dst != null ? !dst.equals(testCase.dst) : testCase.dst != null) return false;
        if (!expected.equals(testCase.expected)) return false;
        if (!got.equals(testCase.got)) return false;
        if (plan != null ? !plan.equals(testCase.plan) : testCase.plan != null) return false;
        if (src != null ? !src.equals(testCase.src) : testCase.src != null) return false;
        if (!verifier.toString().equals(testCase.verifier.toString())) return false;

        return true;
    }

    public boolean falsePositive() {
        return expected.getStatus() == false && got.getStatus() == true;
    }

    public boolean falseNegative() {
        return expected.getStatus() == true && got.getStatus() == false;
    }

    @Override
    public int hashCode() {
        int result = c.hashCode();
        result = 31 * result + (plan != null ? plan.hashCode() : 0);
        result = 31 * result + (src != null ? src.hashCode() : 0);
        result = 31 * result + (dst != null ? dst.hashCode() : 0);
        result = 31 * result + args.hashCode();
        result = 31 * result + expected.hashCode();
        result = 31 * result + got.hashCode();
        result = 31 * result + verifier.hashCode();
        result = 31 * result + (d ? 1 : 0);
        return result;
    }
}
