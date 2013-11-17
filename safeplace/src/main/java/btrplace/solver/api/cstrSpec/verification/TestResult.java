package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;

/**
 * @author Fabien Hermenier
 */
public class TestResult {

    private ReconfigurationPlan p;
    private SatConstraint c;
    private boolean isConsistent;
    private Exception errMsg;
    private ErrorType err;

    private int num;

    public static enum ErrorType {succeed, falseNegative, falsePositive, bug}

    public TestResult(int num, ReconfigurationPlan p, SatConstraint cstr, boolean isConsistent, ErrorType err) {
        this(num, p, cstr, isConsistent, err, null);
    }

    public TestResult(int num, ReconfigurationPlan p, SatConstraint cstr, boolean isConsistent, ErrorType err, Exception errMsg) {
        this.num = num;
        this.p = p;
        this.c = cstr;
        this.isConsistent = isConsistent;
        this.errMsg = errMsg;
        this.err = err;
    }

    public ReconfigurationPlan plan() {
        return p;
    }

    public SatConstraint getConstraint() {
        return c;
    }

    public boolean isConsistent() {
        return isConsistent;
    }

    public Exception errorMessage() {
        return errMsg;
    }

    public ErrorType errorType() {
        return err;
    }

    public boolean succeeded() {
        return err == ErrorType.succeed;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("Test " + num + " result:\n-------------\n");
        res.append("consistent: ").append(isConsistent);
        res.append(", result: ").append(err);
        if (!succeeded()) {
            res.append("\nconstraint: ").append(c).append('\n');
            res.append("error: ").append(errMsg).append('\n');
            res.append("origin:\n").append(p.getOrigin().getMapping()).append('\n');
            res.append("plan:\n").append(p);
        }
        return res.toString();

    }
}
