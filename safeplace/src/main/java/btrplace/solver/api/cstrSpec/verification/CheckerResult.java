package btrplace.solver.api.cstrSpec.verification;

import btrplace.plan.event.Action;

import java.util.Objects;

/**
 * @author Fabien Hermenier
 */
public class CheckerResult {

    private Boolean b;

    private String ex;

    private static final CheckerResult success = new CheckerResult(true, "");

    public CheckerResult(Boolean b, Action a) {
        this.b = b;
        this.ex = a.toString();
    }


    public CheckerResult(Boolean b, String ex) {
        this.b = b;
        this.ex = ex;
    }

    public static CheckerResult newSuccess() {
        return success;
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
            return b.toString();
        }
        return b + " (" + ex + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CheckerResult that = (CheckerResult) o;

        if (!b.equals(that.b)) return false;
        if (ex != null ? !ex.equals(that.ex) : that.ex != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(b, ex);
    }
}
