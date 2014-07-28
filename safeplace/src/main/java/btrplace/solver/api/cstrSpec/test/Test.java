package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.CTestCaseResult;
import btrplace.solver.api.cstrSpec.runner.CTestCaseReport;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;
import btrplace.solver.api.cstrSpec.runner.TestsScanner;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Test {

    public static void main(String[] args) throws Exception {
        long st = System.currentTimeMillis();
        TestsScanner scanner = new TestsScanner();
        scanner.restrictToTest("TestBan");
        List<CTestCasesRunner> runners = scanner.scan();

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
        long ed = System.currentTimeMillis();
        System.out.println("\nTests run: " + (ok + fp + fn) + "; F/P: " + fp + ", F/N: " + fn + " (" + (ed - st) + " ms)");
    }
}
