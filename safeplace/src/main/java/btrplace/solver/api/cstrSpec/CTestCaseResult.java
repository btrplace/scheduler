package btrplace.solver.api.cstrSpec;

/**
 * @author Fabien Hermenier
 */
public class CTestCaseResult {

    private String id;

    public static enum Result {success, falsePositive, falseNegative}

    private Result res;

    private CTestCase tc;

    public CTestCaseResult(String id, CTestCase tc, Result r) {
        res = r;
        this.id = id;
        this.tc = tc;
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
}
