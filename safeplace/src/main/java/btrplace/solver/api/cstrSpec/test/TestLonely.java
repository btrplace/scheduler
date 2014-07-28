package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.annotations.CstrTestsProvider;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestLonely {

    @CstrTestsProvider(name = "foo", constraint = "split")
    public ReconfigurationPlanFuzzer2 foo() {
        return new ReconfigurationPlanFuzzer2();
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
