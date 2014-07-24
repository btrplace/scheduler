package btrplace.solver.api.cstrSpec.guard;

import btrplace.solver.api.cstrSpec.CTestCaseResult;
import btrplace.solver.api.cstrSpec.verification.TestCase;

/**
 * @author Fabien Hermenier
 */
public class TimeGuard implements Guard {

    private int d;

    private long start = -1;

    public TimeGuard(int s) {
        d = s;
    }

    @Override
    public boolean acceptDefiant(TestCase tc) {
        return checkTimer();
    }

    @Override
    public boolean acceptCompliant(TestCase tc) {
        return checkTimer();
    }

    private boolean checkTimer() {
        if (start == -1) {
            start = System.currentTimeMillis();
        }
        return System.currentTimeMillis() < start + d * 1000;
    }

    @Override
    public boolean accept(CTestCaseResult r) {
        return checkTimer();
    }
}
