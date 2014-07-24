package btrplace.solver.api.cstrSpec;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class CTestCase {

    private String id;

    private Constraint cstr;

    private List<Constant> args;

    private ReconfigurationPlan plan;

    public CTestCase(String id, Constraint c, List<Constant> argv, ReconfigurationPlan p) {
        cstr = c;
        this.id = id;
        args = argv;
        plan = p;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("id: ").append(id).append("\n");
        b.append("Constraint: ").append(cstr.toString(args)).append("\n");
        b.append("Origin:\n").append(plan.getOrigin().getMapping());
        b.append("Plan:\n").append(plan);
        return b.toString();
    }

    public Constraint getConstraint() {
        return cstr;
    }

    public List<Constant> getParameters() {
        return args;
    }

    public ReconfigurationPlan getPlan() {
        return plan;
    }

    public String id() {
        return id;
    }
}
