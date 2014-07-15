package btrplace.solver.api.cstrSpec.backend;

import btrplace.solver.api.cstrSpec.verification.TestCase;

/**
 * @author Fabien Hermenier
 */
public class NoBackend implements VerificationBackend {

    @Override
    public void addDefiant(TestCase c) {

    }

    @Override
    public void addCompliant(TestCase c) {

    }

    @Override
    public void flush() {
    }
}
