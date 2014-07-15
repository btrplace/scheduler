package btrplace.solver.api.cstrSpec.backend;

import btrplace.solver.api.cstrSpec.verification.TestCase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Fabien Hermenier
 */
public class Counting implements VerificationBackend {

    private AtomicInteger nbDefiant;

    private AtomicInteger nbCompliant;

    public Counting() {
        nbDefiant = new AtomicInteger(0);
        nbCompliant = new AtomicInteger(0);
    }

    @Override
    public void addDefiant(TestCase c) {
        nbDefiant.incrementAndGet();
    }

    @Override
    public void addCompliant(TestCase c) {
        nbCompliant.incrementAndGet();
    }

    @Override
    public void flush() {
    }

    public AtomicInteger getNbDefiant() {
        return nbDefiant;
    }

    public AtomicInteger getNbCompliant() {
        return nbCompliant;
    }
}
