package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.fuzzer.ShareableResourceFuzzer;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;
import btrplace.solver.api.cstrSpec.verification.spec.FloatVerifDomain;
import btrplace.solver.api.cstrSpec.verification.spec.StringEnumVerifDomain;

/**
 * @author Fabien Hermenier
 */
public class TestOverbook {

    private static String[] rcDoms = new String[]{"cpu", "mem"};

    private ShareableResourceFuzzer rcf = new ShareableResourceFuzzer("cpu", 0, 7, 3, 5);

    private static CTestCasesRunner customize(CTestCasesRunner r) {
        r.dom(new StringEnumVerifDomain(rcDoms))
                .dom(new FloatVerifDomain(1, 2, 0.25));
        return r;
    }

    @CstrTest(constraint = "overbook", groups = {"rc", "unit"})
    public void testContinuous(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.continuous()));
    }

    @CstrTest(constraint = "overbook", groups = {"rc", "unit"})
    public void testContinuousRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.continuous())).impl().repair(true);
    }

    @CstrTest(constraint = "overbook", groups = {"rc", "unit"})
    public void testDiscrete(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.discrete()));
    }

    @CstrTest(constraint = "overbook", groups = {"rc", "unit"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        TestUtils.quickCheck(customize(r.discrete())).impl().repair(true);
    }

}
