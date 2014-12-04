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

package org.btrplace.safeplace.runner;

import eu.infomas.annotation.AnnotationDetector;
import org.btrplace.safeplace.Specification;
import org.btrplace.safeplace.annotations.CstrTest;
import org.btrplace.safeplace.fuzzer.ReconfigurationPlanFuzzer;
import org.btrplace.safeplace.fuzzer.ReconfigurationPlanFuzzer2;
import org.btrplace.safeplace.spec.SpecExtractor;

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


    private Map<String, ReconfigurationPlanFuzzer> fuzzerCache;
    private List<String> tests, grps;

    private static final ReconfigurationPlanFuzzer DEFAULT_FUZZER = new ReconfigurationPlanFuzzer2();
    private List<CTestCasesRunner> runners;

    private Specification spec;

    public TestsScanner() throws Exception {
        tests = new ArrayList<>();
        grps = new ArrayList<>();
        runners = new ArrayList<>();
        fuzzerCache = new HashMap<>();
        SpecExtractor ex = new SpecExtractor();
        spec = ex.extract();
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
                System.out.println(cl);
                return;
            }
            runner = new CTestCasesRunner(spec, cl, methodName, cstr);
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

    private ReconfigurationPlanFuzzer getProvider(Object o, String lbl) throws Exception {

        ReconfigurationPlanFuzzer f = fuzzerCache.get(lbl);
        if (f != null) {
            return f;
        }
        try {
            Method m = o.getClass().getMethod(lbl);
            f = (ReconfigurationPlanFuzzer) m.invoke(o);
            fuzzerCache.put(lbl, f);
            return (ReconfigurationPlanFuzzer) m.invoke(o);
        } catch (NoSuchMethodException ex) {
            throw new Exception("Unknown provider '" + lbl + "'");
        }
    }


    public List<CTestCasesRunner> scan() throws Exception {
        AnnotationDetector detector = new AnnotationDetector(this);
        detector.detect();
        return runners;
    }
}
