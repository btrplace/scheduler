package btrplace.solver.api.cstrSpec.guard;

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
        return m.getAndDecrement() > 0;
    }

    @Override
    public boolean acceptCompliant(TestCase tc) {
        return true;
    }
}
