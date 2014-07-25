package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.runner.CTestCaseReport;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;
import btrplace.solver.api.cstrSpec.runner.TestsScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Test {

    private static void reportError(CTestCaseResult res) {
        System.out.println(res.id() + " " + res.result());
    }

    public static void main(String[] args) throws Exception {

        /*
        Failed tests:
  ComTest.testExecuteEascActivityPlan:64 expected:<200> but was:<204>

        Test run: X, Failures: Y, Errors: Z
         */
        TestsScanner scanner = new TestsScanner();
        List<CTestCasesRunner> runners = scanner.scan();
        List<CTestCaseReport> reports = new ArrayList<>();

        boolean errHeader = false;
        int ok = 0, fp = 0, fn = 0;
        for (CTestCasesRunner runner : runners) {
            CTestCaseReport report = new CTestCaseReport(runner.id());
            report.report(runner.report());
            for (CTestCaseResult res : runner) {
                report.add(res);
            }
            ok += report.ok();
            fp += report.fp();
            fn += report.fn();
            reports.add(report);
            if (report.report() != null || report.fn() > 0 || report.fp() > 0) {
                if (!errHeader) {
                    System.out.println("Failed tests:");
                    errHeader = true;
                }
                System.out.println(report.pretty());
            }
        }

        if (!errHeader) {
            System.out.println("SUCCESS !");
        }
        System.out.println("\nTests run: " + (ok + fp + fn) + "; F/P: " + fp + ", F/N: " + fn);
    }
}
