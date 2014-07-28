package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.annotations.CstrTestsProvider;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestBan {

    @CstrTestsProvider(name = "foo", constraint = "ban")
    public ReconfigurationPlanFuzzer2 foo() {
        return new ReconfigurationPlanFuzzer2();
    }

    @CstrTest(provider = "foo")
    public void testDiscrete(CTestCasesRunner r) {
        r.discrete().timeout(5).maxTests(1000);
    }

    @CstrTest(provider = "foo")
    public void testDiscreteRepair(CTestCasesRunner r) {
        r.discrete().repair().timeout(5).maxTests(1000);
    }

/*    @CstrTest(input = "foo")
    public void testFoo(CTestCasesRunner r) {
        r.discrete().repair().timeout(5).maxTests(1000);
    }*/

}
