package btrplace.solver.api.cstrSpec.verification;

/**
 * @author Fabien Hermenier
 */
public class CheckerResult {

    private Boolean b;

    private String ex;

    public CheckerResult(Boolean b, String ex) {
        this.b = b;
        this.ex = ex;
    }

    public static CheckerResult newSucess() {
        return new CheckerResult(true, null);
    }

    public static CheckerResult newFailure() {
        return new CheckerResult(false, null);
    }

    public static CheckerResult newFailure(String ex) {
        return new CheckerResult(false, ex);
    }

    public static CheckerResult newError(String ex) {
        return new CheckerResult(null, ex);
    }

    public Boolean getStatus() {
        return b;
    }

    public String getException() {
        return ex;
    }

    @Override
    public String toString() {
        if (ex == null) {
            return "" + b;
        }
        return b + " " + ex;
    }
}
