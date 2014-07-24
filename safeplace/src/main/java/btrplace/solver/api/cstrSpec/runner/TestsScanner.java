package btrplace.solver.api.cstrSpec.runner;

import btrplace.solver.api.cstrSpec.CTestCase;
import btrplace.solver.api.cstrSpec.Specification;
import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.annotations.CstrTestsProvider;
import btrplace.solver.api.cstrSpec.fuzzer.CTestCaseFuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.CTestsCaseInput;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.solver.api.cstrSpec.spec.SpecReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TestsScanner {


    public TestsScanner() {

    }

    private CTestCaseFuzzer getProvider(String lbl, Object o) throws Exception {
        SpecReader r = new SpecReader();
        Specification spec = r.getSpecification(new File("src/main/cspec/v1.cspec"));
        Method m = o.getClass().getMethod(lbl);
        CstrTestsProvider provAn = m.getAnnotation(CstrTestsProvider.class);
        return new CTestCaseFuzzer(provAn.name(), spec.get(provAn.constraint()), (ReconfigurationPlanFuzzer2) m.invoke(o));
    }

    public List<CTestCasesRunner> scan(Object o) throws Exception {

        List<CTestCasesRunner> runners = new ArrayList<>();
        for (Class c = o.getClass(); c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                CstrTest cc = m.getAnnotation(CstrTest.class);
                if (cc != null) {
                    CTestCasesRunner runner = new CTestCasesRunner(m.getName());
                    m.invoke(o, runner);
                    Iterator<CTestCase> in;
                    if (cc.provider().length() > 0) {
                        in = getProvider(cc.provider(), o);
                    } else if (cc.input().length() > 0) {
                        InputStream s;
                        if (new File(cc.input()).exists()) {
                            s = new FileInputStream(cc.input());
                        } else {
                            s = new ByteArrayInputStream(new byte[0]);
                        }
                        in = new CTestsCaseInput(s);
                    } else {
                        throw new RuntimeException("No inputs for tests " + m.getName());
                    }
                    runner.setIn(in);
                    runners.add(runner);
                }
            }
        }
        return runners;
    }

}
