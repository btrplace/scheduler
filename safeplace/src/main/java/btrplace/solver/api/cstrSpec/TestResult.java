package btrplace.solver.api.cstrSpec;

import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;

/**
 * @author Fabien Hermenier
 */
class TestResult {

    private ReconfigurationPlan p;
    private SatConstraint c;
    private boolean isConsistent;
    private String errMsg;
    private ErrorType rc, ri;

    public static enum ErrorType {succeed, falseNegative, falsePositive, bug}

    public TestResult(ReconfigurationPlan p, SatConstraint cstr, boolean isConsistent, ErrorType rc, ErrorType ri) {
        this(p, cstr, isConsistent, rc, ri, null);
    }

    public TestResult(ReconfigurationPlan p, SatConstraint cstr, boolean isConsistent, ErrorType rc, ErrorType ri, String errMsg) {
        this.p = p;
        this.c = cstr;
        this.isConsistent = isConsistent;
        this.errMsg = errMsg;
        this.rc = rc;
        this.ri = ri;
    }

    public ReconfigurationPlan getReconfigurationPlan() {
        return p;
    }

    public SatConstraint getConstraint() {
        return c;
    }

    public boolean isConsistent() {
        return isConsistent;
    }

    public String getErrorMessage() {
        return errMsg;
    }

    public ErrorType getCheckerError() {
        return rc;
    }

    public ErrorType getImplError() {
        return ri;
    }

    public boolean succeeded() {
        return ri == ErrorType.succeed && rc == ErrorType.succeed;
    }

    @Override
    public String toString() {
        if (rc == ErrorType.succeed && ri == ErrorType.succeed && errMsg == null) {
            return "checker: " + rc + "\timpl: " + ri;
        } else {
            return "checker: " + rc + "\timpl: " + ri + "\n" +
                    "cstr: " + c + "\n"
                    + "Mapping:\n" + p.getOrigin().getMapping() + "\n"
                    + "plan:\n" + p
                    + "Error: " + errMsg;
        }
    }
}
