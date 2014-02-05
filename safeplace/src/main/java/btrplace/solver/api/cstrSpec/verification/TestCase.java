package btrplace.solver.api.cstrSpec.verification;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TestCase {

    private Constraint c;

    private ReconfigurationPlan plan;

    private List<Constant> args;

    private List<Verifier> verifs;

    private List<CheckerResult> res;

    private boolean d;

    public TestCase(Constraint c, ReconfigurationPlan p, List<Constant> args, boolean d) {
        this(Arrays.asList(new ImplVerifier()/*, new CheckerVerifier()*/, new SpecVerifier()), c, p, args, d);
    }

    public TestCase(List<Verifier> verifs, Constraint c, ReconfigurationPlan p, List<Constant> args, boolean d) {
        this.verifs = verifs;
        res = new ArrayList<>(verifs.size());
        for (Verifier v : verifs) {
            res.add(v.verify(c, p, args, d));
        }

        this.args = args;
        this.c = c;
        this.plan = p;
    }

    public List<Verifier> getVerifiers() {
        return verifs;
    }

    public boolean succeed() {
        boolean st = res.get(0).getStatus();
        for (int i = 1; i < res.size(); i++) {
            if (res.get(i).getStatus() != st) {
                return false;
            }
        }
        return true;
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

    public List<CheckerResult> getResults() {
        return res;
    }

    public boolean isDiscrete() {
        return d;
    }

    public String pretty() {
        StringBuilder b = new StringBuilder();
        if (d) {
            b.append("discrete ");
        }
        b.append(c.toString(args)).append(" ");
        b.append(succeed()).append("\n");
        for (int i = 0; i < res.size(); i++) {
            b.append("\t").append(verifs.get(i).getClass().getSimpleName()).append(": ").append(res.get(i)).append("\n");
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
