package btrplace.solver.api.cstrSpec;

/**
 * @author Fabien Hermenier
 */
public class CTestCaseResult {

    private String id;

    public static enum Result {success, falsePositive, falseNegative}

    private Result res;

    private String stdout;

    private String stderr;

    public CTestCaseResult(String id, Result r) {
        res = r;
        this.id = id;
        stdout = "";
        stderr = "";
    }

    public void setStdout(String s) {
        stdout = s;
    }

    public void setStderr(String s) {
        stderr = s;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("id: ").append(id).append("\n");
        b.append("res: ").append(res).append("\n");
        return b.toString();
    }

    public Result result() {
        return res;
    }

    public String id() {
        return id;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }
}
