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

    private static String[] rcDoms = new String[]{"cpu"};

    private ShareableResourceFuzzer rcf = new ShareableResourceFuzzer("cpu", 1, 7, 3, 5);

    @CstrTestsProvider(name = "preserve")
    public ReconfigurationPlanFuzzer2 myProvider() {
        return new ReconfigurationPlanFuzzer2().viewFuzzer(rcf, new ShareableResourceConverter());
    }

    private static CTestCasesRunner customize(CTestCasesRunner r) {
        r.dom(new StringEnumVerifDomain(rcDoms))
                .dom(new IntVerifDomain(1, 7));
        return r;
    }
    /*@CstrTest(constraint = "preserve", groups = {"rc", "long"})
    public void testContinuous(CTestCasesRunner r) {
        TestUtils.longCheck(customize(r.continuous()));
    }

    @CstrTest(constraint = "preserve", groups = {"rc", "long"})
    public void testContinuousRepair(CTestCasesRunner r) {
        TestUtils.longCheck(customize(r.continuous())).impl().repair(true);
    }       */

    @CstrTest(provider = "myProvider", constraint = "preserve", groups = {"rc", "long"})
    public void testDiscrete(CTestCasesRunner r) {
        TestUtils.longCheck(customize(r.discrete()));
    }

    @CstrTest(provider = "myProvider", constraint = "preserve", groups = {"rc", "long"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        TestUtils.longCheck(customize(r.discrete())).impl().repair(true);
    }

}
