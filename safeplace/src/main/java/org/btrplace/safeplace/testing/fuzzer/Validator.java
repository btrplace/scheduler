/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

import java.util.List;
import java.util.function.Predicate;

/**
 * This object allows to check if a test case validates some constraints that are preconditions.
 *
 * @author Fabien Hermenier
 */
public class Validator implements Predicate<TestCase> {

    private List<Constraint> cstrs;

    private Tester tester;

    private long duration;

    /**
     * New Validator.
     *
     * @param t   the tester to use to check for the validity
     * @param pre the constraints to consider as preconditions
     */
    public Validator(Tester t, List<Constraint> pre) {
        cstrs = pre;
        tester = t;
    }

    @Override
    public boolean test(TestCase o) {
        duration = -System.currentTimeMillis();

        try {
            for (Constraint c : cstrs) {
                TestCase tc = purge(o, c);
                TestCaseResult res = tester.test(tc);
                if (res.result() != Result.SUCCESS) {
                    return false;
                }
            }
        } finally {
            duration += System.currentTimeMillis();
        }
        return true;
    }

    private TestCase purge(TestCase o, Constraint c) {
        //TODO: Get rid of all the views (optional so subject to un-awaited fault injection
        return new TestCase(o.instance(), o.plan(), c);
    }

    /**
     * Get the duration of the validation phase.
     * @return a duration in milliseconds
     */
    public long lastDuration() {
        return duration;
    }
}
