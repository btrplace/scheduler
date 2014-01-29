package btrplace.solver.api.cstrSpec.verification;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.ConstraintCall;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TestCase2 {

    private ReconfigurationPlan plan;

    private Constraint cstr;

    private List<Constant> in;

    public TestCase2(ReconfigurationPlan p, Constraint cstr, List<Constant> in) {
        this.plan = p;
        this.cstr = cstr;
        this.in = in;
    }

    public ReconfigurationPlan getPlan() {
        return plan;
    }

    public Constraint getConstraint() {
        return cstr;
    }

    public List<Constant> getInputs() {
        return in;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("constraint: ").append(new ConstraintCall(cstr, (List) in)).append("\n");
        b.append("origin:\n").append(plan.getOrigin().getMapping()).append("\n");
        b.append("plan:\n").append(plan).append("\n");
        return b.toString();
    }
}

