package btrplace.solver.api.cstrSpec;

import btrplace.model.view.ModelView;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;

/**
 * @author Fabien Hermenier
 */
public class CTestCaseResult {

    public static enum Result {success, falsePositive, falseNegative}

    private Result res;

    private String stdout;

    private String stderr;

    private CheckerResult res1, res2;

    private CTestCase tc;

    public CTestCaseResult(CTestCase tc, CheckerResult r1, CheckerResult r2) {
        stdout = "";
        stderr = "";
        res1 = r1;
        res2 = r2;
        this.tc = tc;
        res = makeResult(res1, res2);
    }

    private Result makeResult(CheckerResult res1, CheckerResult res2) {
        if (res1.getStatus().equals(res2.getStatus())) {
            return CTestCaseResult.Result.success;
        }

        if (res1.getStatus()) {
            return CTestCaseResult.Result.falseNegative;
        }
        return CTestCaseResult.Result.falsePositive;
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
        b.append("id: ").append(tc.id()).append("\n");
        b.append("constraint: ").append(tc.getConstraint().toString(tc.getParameters())).append("\n");
        b.append("specRes: ").append(res1).append("\n");
        b.append("vRes: ").append(res2).append("\n");
        b.append("res: ").append(res).append("\n");
        b.append("origin:\n").append(tc.getPlan().getOrigin().getMapping());
        if (!tc.getPlan().getOrigin().getViews().isEmpty()) {
            for (ModelView v : tc.getPlan().getOrigin().getViews()) {
                b.append("view " + v.getIdentifier() + ": " + v + "\n");
            }
        }
        b.append("actions:\n").append(tc.getPlan());
        return b.toString();
    }

    public Result result() {
        return res;
    }

    public CTestCase getTestCase() {
        return tc;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }
}
