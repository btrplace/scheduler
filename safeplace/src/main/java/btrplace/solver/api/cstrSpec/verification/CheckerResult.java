package btrplace.solver.api.cstrSpec.verification;

/**
 * @author Fabien Hermenier
 */
public class CheckerResult {

    private Boolean b;

    private Exception ex;

    public CheckerResult(Boolean b, Exception ex) {
        this.b = b;
        this.ex = ex;
    }

    public static CheckerResult newSucess() {
        return new CheckerResult(true, null);
    }

    public static CheckerResult newFailure() {
        return new CheckerResult(false, null);
    }

    public static CheckerResult newFailure(Exception ex) {
        return new CheckerResult(false, ex);
    }

    public static CheckerResult newError(Exception ex) {
        return new CheckerResult(null, ex);
    }

    public Boolean getStatus() {
        return b;
    }

    public Exception getException() {
        return ex;
    }

    @Override
    public String toString() {
        if (ex == null) {
            return "" + b;
        }
        return b + " " + ex.getMessage();
    }
}
