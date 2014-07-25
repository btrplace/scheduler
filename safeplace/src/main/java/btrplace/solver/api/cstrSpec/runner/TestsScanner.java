package btrplace.solver.api.cstrSpec.runner;

import btrplace.solver.api.cstrSpec.CTestCase;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.Specification;
import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.annotations.CstrTestsProvider;
import btrplace.solver.api.cstrSpec.fuzzer.CTestCaseFuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.CTestsCaseInput;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import eu.infomas.annotation.AnnotationDetector;

import java.io.File;
import java.io.FileInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TestsScanner implements AnnotationDetector.MethodReporter {


    private List<CTestCasesRunner> runners;

    public TestsScanner() {
        runners = new ArrayList<>();
    }

    @Override
    public Class<? extends Annotation>[] annotations() {
        return new Class[]{CstrTest.class};
    }

    @Override
    public void reportMethodAnnotation(Class<? extends Annotation> annotation, String className, String methodName) {
        CTestCasesRunner runner = new CTestCasesRunner(className.substring(className.lastIndexOf(".") + 1) + "." + methodName);
        runners.add(runner);
        try {
            Class cl = Class.forName(className);
            Object o = cl.newInstance();
            Method m = cl.getMethod(methodName, CTestCasesRunner.class);
            CstrTest cc = m.getAnnotation(CstrTest.class);
            m.invoke(o, runner);
            Iterator<CTestCase> in;
            if (cc.provider().length() > 0) {
                runner.setIn(getProvider(o, cc.provider()));
            } else if (cc.input().length() > 0) {
                if (new File(cc.input()).exists()) {
                    runner.setIn(new CTestsCaseInput(new FileInputStream(cc.input())));
                } else {
                    runner.report(new IllegalArgumentException("unknown input file '" + cc.input() + "'"));
                }

            } else {
                runner.report(new IllegalArgumentException("No inputs"));
            }

        } catch (Exception e) {
            runner.report(e);
        }
    }


    private CTestCaseFuzzer getProvider(Object o, String lbl) throws Exception {
        SpecReader r = new SpecReader();
        //TODO Groumph, hardcoded
        Specification spec = r.getSpecification(new File("src/main/cspec/v1.cspec"));
        try {
            Method m = o.getClass().getMethod(lbl);
            CstrTestsProvider provAn = m.getAnnotation(CstrTestsProvider.class);
            Constraint cstr = spec.get(provAn.constraint());
            return new CTestCaseFuzzer(provAn.name(), cstr, makePreconditions(cstr, spec), (ReconfigurationPlanFuzzer2) m.invoke(o));
        } catch (NoSuchMethodException ex) {
            throw new Exception("Unknown provider '" + lbl + "'");
        }
    }

    private List<Constraint> makePreconditions(Constraint c, Specification spec) {
        List<Constraint> pre = new ArrayList<>();
        for (Constraint x : spec.getConstraints()) {
            if (x.isCore()) {
                pre.add(x);
            }
        }
        pre.remove(c);
        return pre;
    }

    public List<CTestCasesRunner> scan() throws Exception {

        AnnotationDetector detector = new AnnotationDetector(this);
        detector.detect();
        return runners;
    }

}
