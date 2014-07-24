package btrplace.solver.api.cstrSpec.runner;

import btrplace.solver.api.cstrSpec.CTestCaseResult;

/**
 * @author Fabien Hermenier
 */
public class CTestCaseReport {

    private String id;

    private int ok = 0, fp = 0, fn = 0;

    public CTestCaseReport(String id) {
        this.id = id;
    }

    public void add(CTestCaseResult r) {
        switch (r.result()) {
            case success:
                ok++;
                break;
            case falseNegative:
                fn++;
                break;
            case falsePositive:
                fp++;
                break;
        }
    }

    public String pretty() {
        return id + ": " + (ok + fn + fp) + " test(s); " + fp + " F/P; " + fn + " F/N";
    }

    public int ok() {
        return ok;
    }

    public int fp() {
        return fp;
    }

    public int fn() {
        return fn;
    }
}
