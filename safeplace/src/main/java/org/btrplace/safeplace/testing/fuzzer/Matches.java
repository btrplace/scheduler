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

package org.btrplace.safeplace.testing.fuzzer;

import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.testing.Result;
import org.btrplace.safeplace.testing.TestCase;
import org.btrplace.safeplace.testing.TestCaseResult;
import org.btrplace.safeplace.testing.Tester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Fabien Hermenier
 */
public class Matches implements Predicate<TestCase> {

    private List<Constraint> cstrs;

    private Tester tester;

    private long duration;

    public Matches(Tester t) {
        cstrs = new ArrayList<>();
        tester = t;
    }

    public Matches() {
        this(null);
    }

    public Matches with(Constraint ...cstrs) {
        Collections.addAll(this.cstrs, cstrs);
        return this;
    }

    public Matches setTester(Tester t) {
        tester = t;
        return this;
    }
    @Override
    public boolean test(TestCase o) {
        duration = -System.currentTimeMillis();

        if (tester != null) {
            for (Constraint c : cstrs) {
                TestCase tc = new TestCase(o.instance(), o.plan(), c);
                TestCaseResult res = tester.test(tc);
                if (res.result() != Result.success) {
                    duration += System.currentTimeMillis();
                    System.out.println("Fail for " + c.id());
                /*System.out.println(tc.instance().getSatConstraints());
                System.out.println(tc.instance().getModel().getMapping());
                System.out.println(tc.plan());
                System.out.println("----");*/
                    return false;
                } else {
                    System.out.println("Ok for " + c.id());
                }
            }
        }
        duration += System.currentTimeMillis();
        return true;
    }

    public long lastDuration() {
        return duration;
    }
}
