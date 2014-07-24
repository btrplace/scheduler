package btrplace.solver.api.cstrSpec;

/**
 * @author Fabien Hermenier
 */
public class CTestCaseResult {

    private String id;

    public static enum Result {success, falsePositive, falseNegative}

    private Result res;

    private CTestCase tc;

    private String stdout;

    private String stderr;

    public CTestCaseResult(String id, CTestCase tc, Result r) {
        res = r;
        this.id = id;
        this.tc = tc;
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
        b.append("test case: ").append(tc.id());
        return b.toString();
    }

    public Result result() {
        return res;
    }

    public String id() {
        return id;
    }
}
