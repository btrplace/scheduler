package btrplace.solver.api.cstrSpec.runner;

import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.Specification;
import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.annotations.CstrTestsProvider;
import btrplace.solver.api.cstrSpec.fuzzer.CTestCaseFuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import eu.infomas.annotation.AnnotationDetector;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class TestsScanner implements AnnotationDetector.MethodReporter {


    private Map<String, CTestCaseFuzzer> fuzzerCache;
    private List<String> tests, grps;

    private List<CTestCasesRunner> runners;

    public TestsScanner() {
        tests = new ArrayList<>();
        grps = new ArrayList<>();
        runners = new ArrayList<>();
        fuzzerCache = new HashMap<>();
    }

    public void restrictToGroup(String g) {
        grps.add(g);
    }

    public void restrictToTest(String t) {
        tests.add(t);
    }

    @Override
    public Class<? extends Annotation>[] annotations() {
        return new Class[]{CstrTest.class};
    }

    @Override
    public void reportMethodAnnotation(Class<? extends Annotation> annotation, String className, String methodName) {
        CTestCasesRunner runner = new CTestCasesRunner(className.substring(className.lastIndexOf(".") + 1) + "." + methodName);
        //TODO: constraint parsing, preconditions, doms, cstrs++
        try {
            Class cl = Class.forName(className);
            Method m = cl.getMethod(methodName, CTestCasesRunner.class);
            CstrTest cc = m.getAnnotation(CstrTest.class);
            if (!match(cl, cc)) {
                return;
            }
            //System.out.println(cl.getSimpleName() + " " + Arrays.toString(cc.groups())+ " match " + grps + " " + tests);
            runners.add(runner);
            Object o = cl.newInstance();

            m.invoke(o, runner);
            runner.setIn(getProvider(o, cc.provider()));

        } catch (Exception e) {
            runner.report(e);
        }
    }

    private boolean match(Class cl, CstrTest cc) {
        if (!tests.isEmpty()) {
            if (tests.contains(cl.getSimpleName())) {
                return true;
            }
        }
        if (!grps.isEmpty()) {
            for (String g : cc.groups()) {
                if (grps.contains(g)) {
                    return true;
                }
            }
            return false;
        }
        return tests.isEmpty();
    }

    private CTestCaseFuzzer getProvider(Object o, String lbl) throws Exception {

        CTestCaseFuzzer f = fuzzerCache.get(lbl);
        if (f != null) {
            return f;
        }
        SpecReader r = new SpecReader();
        //TODO Groumph, hardcoded
        Specification spec = r.getSpecification(new File("src/main/cspec/v1.cspec"));
        try {
            Method m = o.getClass().getMethod(lbl);
            CstrTestsProvider provAn = m.getAnnotation(CstrTestsProvider.class);
            Constraint cstr = spec.get(provAn.constraint());
            f = new CTestCaseFuzzer(provAn.name(), cstr, makePreconditions(cstr, spec), (ReconfigurationPlanFuzzer2) m.invoke(o));
            fuzzerCache.put(lbl, f);
            return f;
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
