/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.safeplace.runner;

import btrplace.safeplace.Constraint;
import btrplace.safeplace.Specification;
import btrplace.safeplace.annotations.CstrTest;
import btrplace.safeplace.fuzzer.ReconfigurationPlanFuzzer2;
import eu.infomas.annotation.AnnotationDetector;

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


    private Map<String, ReconfigurationPlanFuzzer2> fuzzerCache;
    private List<String> tests, grps;

    private static final ReconfigurationPlanFuzzer2 DEFAULT_FUZZER = new ReconfigurationPlanFuzzer2();
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
        CTestCasesRunner runner = null;
        String cstr;
        try {
            Class cl = Class.forName(className);
            Method m = cl.getMethod(methodName, CTestCasesRunner.class);
            CstrTest cc = m.getAnnotation(CstrTest.class);
            cstr = cc.constraint();
            if (!match(cl, cc)) {
                return;
            }
            runner = new CTestCasesRunner(cl, methodName, cstr);
            runners.add(runner);
            //TODO: constraint parsing, preconditions, doms, cstrs++

            //System.out.println(cl.getSimpleName() + " " + Arrays.toString(cc.groups())+ " match " + grps + " " + tests);

            Object o = cl.newInstance();

            m.invoke(o, runner);
            if (cc.provider().length() == 0) {
                runner.setIn(new ReconfigurationPlanFuzzer2());
            } else {
                runner.setIn(getProvider(o, cc.provider()));
            }

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

    private ReconfigurationPlanFuzzer2 getProvider(Object o, String lbl) throws Exception {

        ReconfigurationPlanFuzzer2 f = fuzzerCache.get(lbl);
        if (f != null) {
            return f;
        }
        try {
            Method m = o.getClass().getMethod(lbl);
            f = (ReconfigurationPlanFuzzer2) m.invoke(o);
            fuzzerCache.put(lbl, f);
            return (ReconfigurationPlanFuzzer2) m.invoke(o);
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
