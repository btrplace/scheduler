package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.annotations.CstrTestsProvider;
import btrplace.solver.api.cstrSpec.fuzzer.CTestCaseFuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;
import btrplace.solver.api.cstrSpec.spec.SpecReader;

import java.io.File;
import java.lang.reflect.Method;

/**
 * @author Fabien Hermenier
 */
public class Test {

    @CstrTestsProvider(name = "foo", constraint = "spread")
    public ReconfigurationPlanFuzzer2 foo() {
        return new ReconfigurationPlanFuzzer2();
    }

    @CstrTest(provider = "foo")
    public void test(CTestCasesRunner r) {
        r.continuous().timeout(5);
        /*return v.stopAfter(X)
                .maxTests(Y)
                .maxUniqueTests(Y)
                .maxFailure(Z)
                .continuous()
                .discrete()
                .repair
                .rebuild
          */
    }

    private CTestCaseFuzzer getProvider(String lbl, Object o) throws Exception {
        SpecReader r = new SpecReader();
        Specification spec = r.getSpecification(new File("src/main/cspec/v1.cspec"));
        Method m = o.getClass().getMethod(lbl);
        CstrTestsProvider provAn = m.getAnnotation(CstrTestsProvider.class);
        return new CTestCaseFuzzer(provAn.name(), spec.get(provAn.constraint()), (ReconfigurationPlanFuzzer2) m.invoke(o));
    }

    public void catchTests(Object o) throws Exception {
        for (Class c = o.getClass(); c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                CstrTest cc = m.getAnnotation(CstrTest.class);
                if (cc != null) {
                    CTestCasesRunner runner = new CTestCasesRunner(m.getName());
                    System.out.println(m);
                    m.invoke(o, runner);
                    if (cc.provider() != null) {
                        CTestCaseFuzzer provider = getProvider(cc.provider(), o);
                        runner.setIn(provider);
                    } else if (cc.input() != null) {
                        throw new UnsupportedOperationException();
                    } else {
                        throw new RuntimeException("No inputs for tests " + m.getName());
                    }
                    for (CTestCaseResult x : runner) {
                        System.out.println(x + "\n");
                    }
                }
            }
        }

    }

    public static void main(String[] args) throws Exception {
        Test t = new Test();
        t.catchTests(t);
    }
}
