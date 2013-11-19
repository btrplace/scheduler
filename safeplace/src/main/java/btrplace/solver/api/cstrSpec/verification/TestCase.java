package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;

/**
 * @author Fabien Hermenier
 */
public class TestCase {

    private ReconfigurationPlan plan;

    private SatConstraint cstr;

    private boolean isConsistent;

    private int num;

    public TestCase(int num, ReconfigurationPlan p, SatConstraint cstr, boolean c) {
        this.num = num;
        this.plan = p;
        this.cstr = cstr;
        this.isConsistent = c;
    }

    public boolean isConsistent() {
        return this.isConsistent;
    }

    public ReconfigurationPlan getPlan() {
        return plan;
    }

    public SatConstraint getSatConstraint() {
        return this.cstr;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("cstr: ").append(cstr).append("\n");
        b.append("origin:\n").append(plan.getOrigin().getMapping()).append("\n");
        b.append("plan:\n").append(plan).append("\n");
        b.append("consistent:").append(isConsistent);
        return b.toString();
    }

    public int num() {
        return num;
    }
}

