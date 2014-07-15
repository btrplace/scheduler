package btrplace.solver.api.cstrSpec.backend;

import btrplace.solver.api.cstrSpec.verification.TestCase;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Fabien Hermenier
 */
public class InMemoryBackend implements VerificationBackend {

    private BlockingQueue<TestCase> defiant;
    private BlockingQueue<TestCase> compliant;

    public InMemoryBackend() {
        defiant = new LinkedBlockingQueue<>();
        compliant = new LinkedBlockingQueue<>();
    }

    @Override
    public void addDefiant(TestCase c) {
        defiant.add(c);
    }

    @Override
    public void addCompliant(TestCase c) {
        compliant.add(c);
    }

    public BlockingQueue<TestCase> getDefiant() {
        return defiant;
    }

    public BlockingQueue<TestCase> getCompliant() {
        return compliant;
    }

    @Override
    public void flush() {
    }
}
