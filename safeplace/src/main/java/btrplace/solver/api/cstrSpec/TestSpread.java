package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.annotations.CstrTestsProvider;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestSpread {

    @CstrTestsProvider(name = "foo", constraint = "spread")
    public ReconfigurationPlanFuzzer2 foo() {
        return new ReconfigurationPlanFuzzer2();
    }

    @CstrTest(provider = "foo")
    public void testContinuousSpread(CTestCasesRunner r) {
        r.continuous().timeout(5).maxTests(1000);
    }

    @CstrTest(provider = "foo")
    public void testContinuousRepairSpread(CTestCasesRunner r) {
        r.continuous().repair().timeout(5).maxTests(1000);
    }

    @CstrTest(provider = "foo")
    public void testDiscreteSpread(CTestCasesRunner r) {
        r.discrete().timeout(5).maxTests(1000);
    }

    @CstrTest(provider = "foo")
    public void testDiscreteRepairSpread(CTestCasesRunner r) {
        r.discrete().repair().timeout(5).maxTests(1000);
    }
}
