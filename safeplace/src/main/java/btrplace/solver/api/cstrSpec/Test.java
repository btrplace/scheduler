package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.annotations.CstrTestsProvider;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;
import btrplace.solver.api.cstrSpec.runner.TestsScanner;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Test {

    @CstrTestsProvider(name = "foo", constraint = "spread")
    public ReconfigurationPlanFuzzer2 foo() {
        return new ReconfigurationPlanFuzzer2();
    }

    @CstrTest(provider = "foo")
    public void test(CTestCasesRunner r) {
        //save all the test cases in test_foo.json
        //save all the results in test_results.json
        r.continuous().timeout(5).maxTests(10000);
    }

    /*@CstrTest(input = "test.json")
    public void test2(CTestCasesRunner r) {
        r.discrete().maxTests(10).repair();
    } */

    public static void main(String[] args) throws Exception {
        TestsScanner scanner = new TestsScanner();
        List<CTestCasesRunner> runners = scanner.scan(new Test());
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
