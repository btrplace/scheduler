/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

  private final List<Constraint> cstrs;

  private final Tester tester;

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

    private static TestCase purge(TestCase o, Constraint c) {
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
