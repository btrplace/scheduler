/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.safeplace.testing;


import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.btrplace.safeplace.spec.Constraint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Fabien Hermenier
 */
public class TestScanner {

    private List<Constraint> cstrs;


    public TestScanner(List<Constraint> cstrs) {
        this.cstrs = cstrs;
    }


    public List<TestCampaign> test(Method... methods) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        return _test(methods);
    }

    private List<TestCampaign> _test(Method... methods) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        List<TestCampaign> campaigns = new ArrayList<>();
        for (Method m : methods) {
            CstrTest t = m.getAnnotation(CstrTest.class);
            if (t == null) {
                continue;
            }
            Class cl = m.getDeclaringClass();
            Object o = cl.newInstance();
            campaigns.add(makeTestCampaign(m, o));
        }
        return campaigns;
    }

    private TestCampaign makeTestCampaign(Method m, Object o) throws InvocationTargetException, IllegalAccessException {
        TestCampaign campaign = new TestCampaign(cstrs);
        m.invoke(o, campaign);
        return campaign;
    }

    public List<TestCampaign> test(Class... classes) throws IllegalAccessException, InstantiationException, InvocationTargetException {

        List<Method> tests = new ArrayList<>();
        //Grab
        for (Class cl : classes) {
            Arrays.stream(cl.getDeclaredMethods()).filter(
                    m -> m.getAnnotation(CstrTest.class) != null
            ).forEach(tests::add);
        }
        return _test(tests.toArray(new Method[tests.size()]));
    }

    public List<TestCampaign> testGroups(String... groups) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        List<Method> ms = new ArrayList<>();
        Set<String> ok = Stream.of(groups).collect(Collectors.toSet());

        FastClasspathScanner scanner = new FastClasspathScanner();
        scanner.matchClassesWithMethodAnnotation(CstrTest.class, (cl, m) -> {
            CstrTest a = m.getAnnotation(CstrTest.class);
            for (String g : a.groups()) {
                if (ok.contains(g)) {
                    ms.add(m);
                }
            }
        }).scan(Runtime.getRuntime().availableProcessors() - 1);
        return test(ms.toArray(new Method[ms.size()]));
    }
}
