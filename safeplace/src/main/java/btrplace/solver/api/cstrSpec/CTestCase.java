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

    private boolean continuous;

    public CTestCase(String id, Constraint cstr, List<Constant> argv, ReconfigurationPlan p, boolean c) {
        this.cstr = cstr;
        this.id = id;
        args = argv;
        plan = p;
        continuous = c;
    }

    public boolean continuous() {
        return continuous;
    }

    @Override
    public String toString() {
        return "id: " + id + "\n" + "Constraint: " + cstr.toString(args) + "\nContinuous: " + continuous() + "\n" + "Origin:\n" + plan.getOrigin().getMapping() + "Plan:\n" + plan;
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
