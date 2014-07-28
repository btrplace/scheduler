package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.runner.CTestCaseReport;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

import java.util.concurrent.Callable;

/**
 * @author Fabien Hermenier
 */
public class CallableCTestCasesRunner implements Callable<CTestCaseReport> {

    private CTestCasesRunner runner;

    public CallableCTestCasesRunner(CTestCasesRunner r) {
        this.runner = r;
    }

    @Override
    public CTestCaseReport call() throws Exception {
        System.out.println("Started");
        CTestCaseReport report = new CTestCaseReport(runner.id());
        report.report(runner.report());
        for (CTestCaseResult res : runner) {
            report.add(res);
        }
        System.out.println("Finished");
        return report;
    }
}
