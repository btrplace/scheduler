package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;
import btrplace.solver.api.cstrSpec.runner.TestsScanner;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Test {

    public static void main(String[] args) throws Exception {
        TestsScanner scanner = new TestsScanner();
        List<CTestCasesRunner> runners = scanner.scan();
        for (CTestCasesRunner runner : runners) {
            int ok = 0;
            int falsePositives = 0;
            int falseNegatives = 0;
            long st = System.currentTimeMillis();
            for (CTestCaseResult res : runner) {
                CTestCaseResult.Result r = res.result();
                if (r == CTestCaseResult.Result.success) {
                    ok++;
                } else if (r == CTestCaseResult.Result.falseNegative) {
                    falseNegatives++;
                } else if (r == CTestCaseResult.Result.falsePositive) {
                    falsePositives++;
                }
            }
            long ed = System.currentTimeMillis();
            System.out.println(runner.id() + ": " + ok + " success; " + falsePositives + " false positives; " + falseNegatives + " false negatives detected in " + (ed - st) + "ms");
        }
    }
}
