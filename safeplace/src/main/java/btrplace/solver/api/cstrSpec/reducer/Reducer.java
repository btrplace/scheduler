package btrplace.solver.api.cstrSpec.reducer;

import btrplace.solver.api.cstrSpec.verification.TestCase;

/**
 * @author Fabien Hermenier
 */
public interface Reducer {
    TestCase reduce(TestCase tc) throws Exception;
}
