package btrplace.solver.api.cstrSpec.test;

import btrplace.json.model.view.ShareableResourceConverter;
import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.annotations.CstrTestsProvider;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.solver.api.cstrSpec.fuzzer.ShareableResourceFuzzer;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;
import btrplace.solver.api.cstrSpec.verification.spec.IntVerifDomain;
import btrplace.solver.api.cstrSpec.verification.spec.StringEnumVerifDomain;

/**
 * @author Fabien Hermenier
 */
public class TestPreserve {

    private ShareableResourceFuzzer rcf = new ShareableResourceFuzzer("cpu", 1, 7, 3, 5);

    @CstrTestsProvider(name = "preserve")
    public ReconfigurationPlanFuzzer2 myProvider() {
        return new ReconfigurationPlanFuzzer2().viewFuzzer(rcf, new ShareableResourceConverter());
    }

    private static CTestCasesRunner customize(CTestCasesRunner r) {
        return r.dom(new StringEnumVerifDomain(new String[]{"cpu"}))
                .dom(new IntVerifDomain(1, 7));
    }

    @CstrTest(provider = "myProvider", constraint = "preserve", groups = {"rc", "unit"})
    public void testDiscrete(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.discrete()));
    }

    @CstrTest(provider = "myProvider", constraint = "preserve", groups = {"rc", "unit"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.discrete())).impl().repair(true);
    }

    /*@CstrTest(constraint = "preserve", groups = {"rc", "unit"})
    public void testContinuous(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.continuous()));
    }

    @CstrTest(constraint = "preserve", groups = {"rc", "unit"})
    public void testContinuousRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.continuous())).impl().repair(true);
    }       */
}
