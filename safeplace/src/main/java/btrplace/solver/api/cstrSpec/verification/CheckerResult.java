package btrplace.solver.api.cstrSpec.verification;

import btrplace.plan.event.Action;

/**
 * @author Fabien Hermenier
 */
public class CheckerResult {

    private Boolean b;

    private String ex;

    public CheckerResult(Boolean b, Action a) {
        this.b = b;
        this.ex = a.toString();
    }


    public CheckerResult(Boolean b, String ex) {
        this.b = b;
        this.ex = ex;
    }

    public static CheckerResult newSuccess() {
        return new CheckerResult(true, "");
    }

    public static CheckerResult newFailure() {
        return new CheckerResult(false, "");
    }

    public static CheckerResult newFailure(String ex) {
        return new CheckerResult(false, ex);
    }

    public static CheckerResult newFailure(Action a) {
        return new CheckerResult(false, a);
    }

    public Boolean getStatus() {
        return b;
    }

    public String getException() {
        return ex;
    }

    @Override
    public String toString() {
        if (ex == null || ex.length() == 0) {
            return "" + b;
        }
        return b + " (" + ex + ")";
    }
}
