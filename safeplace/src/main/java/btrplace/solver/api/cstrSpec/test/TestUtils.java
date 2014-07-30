package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestUtils {

    public static final CTestCasesRunner quickCheck(CTestCasesRunner r) {
        r.timeout(5);
        r.maxFailures(1);
        return r;
    }

    public static final CTestCasesRunner longCheck(CTestCasesRunner r) {
        //r.maxTests(100);
        r.timeout(10);
        r.maxFailures(1);
        return r;
    }
}
