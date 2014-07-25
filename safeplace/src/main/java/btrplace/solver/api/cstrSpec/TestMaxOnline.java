package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.annotations.CstrTestsProvider;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;
import btrplace.solver.api.cstrSpec.verification.spec.IntVerifDomain;

/**
 * @author Fabien Hermenier
 */
public class TestMaxOnline {

    @CstrTestsProvider(name = "foo", constraint = "maxOnline")
    public ReconfigurationPlanFuzzer2 foo() {
        ReconfigurationPlanFuzzer2 f = new ReconfigurationPlanFuzzer2();
        f.dom(new IntVerifDomain(0, 5));
        return f;
    }

    @CstrTest(provider = "foo")
    public void testContinuous(CTestCasesRunner r) {
        r.continuous().timeout(5).maxTests(1000);
    }

    @CstrTest(provider = "foo")
    public void testContinuousRepair(CTestCasesRunner r) {
        r.continuous().repair().timeout(5).maxTests(1000);
    }

    @CstrTest(provider = "foo")
    public void testDiscrete(CTestCasesRunner r) {
        r.discrete().timeout(5).maxTests(1000);
    }

    @CstrTest(provider = "foo")
    public void testDiscreteRepair(CTestCasesRunner r) {
        r.discrete().repair().timeout(5).maxTests(1000);
    }
}
