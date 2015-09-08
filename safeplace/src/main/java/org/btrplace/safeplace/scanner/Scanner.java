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

package org.btrplace.safeplace.scanner;


import eu.infomas.annotation.AnnotationDetector;
import org.btrplace.safeplace.Constraint;
import org.btrplace.safeplace.Specification;
import org.btrplace.safeplace.fuzzer.Fuzzer;
import org.btrplace.safeplace.fuzzer.FuzzerImpl;
import org.btrplace.safeplace.runner.TestCasesRunner;
import org.btrplace.safeplace.spec.SpecExtractor;
import org.btrplace.safeplace.verification.btrplace.ImplVerifier;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class Scanner implements AnnotationDetector.MethodReporter {

    private List<String> tests, grps;

    private List<TestCasesRunner> runners;

    private Specification spec;

    private Map<String, Fuzzer> sources;

    private Map<TestCasesRunner, String> myFuzzer;

    private Exception ex;

    private List<Constraint> cores;

    public Scanner() {
        tests = new ArrayList<>();
        grps = new ArrayList<>();
        runners = new ArrayList<>();
        myFuzzer = new HashMap<>();
        sources = new HashMap<>();
    }

    public void group(String g) {
        grps.add(g);
    }

    public void test(String t) {
        tests.add(t);
    }

    @Override
    public Class<? extends Annotation>[] annotations() {
        return new Class[]{CstrTest.class, CstrTestsProvider.class};
    }

    @Override
    public void reportMethodAnnotation(Class<? extends Annotation> a, String className, String method) {
        try {
            Class cl = Class.forName(className);
            Object o = cl.newInstance();
            if (a.equals(CstrTestsProvider.class)) {
                Method m = o.getClass().getMethod(method);
                Fuzzer f = (Fuzzer) m.invoke(o);
                CstrTestsProvider cc = m.getAnnotation(CstrTestsProvider.class);
                sources.put(cc.name(), f);
            } else {
                TestCasesRunner runner = null;
                Method m = cl.getMethod(method, TestCasesRunner.class);
                CstrTest cc = m.getAnnotation(CstrTest.class);
                if (!match(cl, cc)) {
                    return;
                }
                Constraint cstr = spec.get(cc.constraint());
                if (cstr == null) {
                    throw new Exception("No constraint '" + cc.constraint() + "'");
                }
                runner = new TestCasesRunner(cstr.isCore() ? Collections.emptyList() : cores, method, cstr);
                m.invoke(o, runner);
                runner.verifier(new ImplVerifier());
                myFuzzer.put(runner, cc.provider());
                runners.add(runner);
            }
        } catch (Exception e) {
            ex = e;
        }

    }

    private boolean match(Class cl, CstrTest cc) {
        if (!tests.isEmpty()) {
            if (tests.contains(cl.getSimpleName())) {
                return true;
            }
        } else if (!grps.isEmpty()) {
            for (String g : cc.groups()) {
                if (grps.contains(g)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public List<TestCasesRunner> scan() throws Exception {
        spec = new SpecExtractor().extract();
        cores = spec.getConstraints().stream().filter(c -> c.isCore()).collect(Collectors.toList());
        AnnotationDetector detector = new AnnotationDetector(this);
        detector.detect();
        if (ex != null) {
            throw ex;
        }
        return runners;
    }

    public Fuzzer fuzzer(TestCasesRunner r) {

        if (!myFuzzer.containsKey(r)) {
            return new FuzzerImpl();
        }
        return sources.get(myFuzzer.get(r));
    }

}
