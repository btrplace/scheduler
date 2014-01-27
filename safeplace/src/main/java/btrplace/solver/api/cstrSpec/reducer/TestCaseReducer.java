package btrplace.solver.api.cstrSpec.reducer;

import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public interface TestCaseReducer {
    List<TestCase> reduce(TestCase c, Constraint cstr, List<Constant> in);
}
