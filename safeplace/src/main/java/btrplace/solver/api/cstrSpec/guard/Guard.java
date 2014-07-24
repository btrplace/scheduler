package btrplace.solver.api.cstrSpec.guard;

import btrplace.solver.api.cstrSpec.CTestCaseResult;
import btrplace.solver.api.cstrSpec.verification.TestCase;

/**
 * @author Fabien Hermenier
 */
public interface Guard {

    boolean acceptDefiant(TestCase tc);

    boolean acceptCompliant(TestCase tc);

    boolean accept(CTestCaseResult r);
}
