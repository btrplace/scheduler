package btrplace.solver.api.cstrSpec.backend;

import btrplace.solver.api.cstrSpec.verification.TestCase;

/**
 * @author Fabien Hermenier
 */
public interface VerificationBackend {

    void addDefiant(TestCase c);

    void addCompliant(TestCase c);

    void flush();
}
