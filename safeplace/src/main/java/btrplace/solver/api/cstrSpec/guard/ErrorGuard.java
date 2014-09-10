package btrplace.solver.api.cstrSpec.guard;

import btrplace.solver.api.cstrSpec.CTestCaseResult;
import btrplace.solver.api.cstrSpec.verification.TestCase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Fabien Hermenier
 */
public class ErrorGuard implements Guard {

    private AtomicInteger m;

    public ErrorGuard(int m) {
        this.m = new AtomicInteger(m);

    }

    @Override
    public boolean acceptDefiant(TestCase tc) {
        return m.decrementAndGet() > 0;
    }

    @Override
    public boolean acceptCompliant(TestCase tc) {
        return true;
    }

    @Override
    public boolean accept(CTestCaseResult r) {
        if (r.result() != CTestCaseResult.Result.success) {
            return m.decrementAndGet() > 0;
        }
        return true;
    }
}
