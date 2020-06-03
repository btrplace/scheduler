/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing.reporting;

import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.testing.Metrics;
import org.btrplace.safeplace.testing.TestCaseResult;

/**
 * A minimal report to count the results.
 *
 * @author Fabien Hermenier
 */
public class Counting implements Report {

    private int under;
    private int over;
    private int failures;
    private int ok;

    private Metrics global;

    private Constraint currentConstraint;

    @SuppressWarnings("squid:S106")
    @Override
    public void with(TestCaseResult r) {
        if (currentConstraint == null || !currentConstraint.id().equals(r.testCase().constraint().id())) {
            currentConstraint = r.testCase().constraint();
        }
        if (global == null) {
            global = r.metrics();
        }
        global = global.plus(r.metrics());
        switch (r.result()) {
            case UNDER_FILTERING:
                under++;
                break;
            case OVER_FILTERING:
                over++;
                break;
            case CRASH:
                failures++;
                break;
            default:
                ok++;
                break;
        }
    }

    @Override
    public int overFiltering() {
        return under;
    }

    @Override
    public int underFiltering() {
        return over;
    }

    @Override
    public int failures() {
        return failures;
    }

    @Override
    public int success() {
        return ok;
    }

    @Override
    public String toString() {
      if (global == null) {
        // We never received a test result.
        return "\t Unable to fuzz a valid state. Consider tuning the fuzzer search space or check the core constraints implementation";
      }
        int qty = ok + failures + over + under;
        StringBuilder b = new StringBuilder();
        //Counters
        b.append(String.format("\t%d success; %d over-filtering; %d under-filtering; %d crash(es)%n", ok, over, under, failures));
        //Average durations
        float fuzzing = 1f * global.fuzzing() / qty;
        float testing = 1f * global.testing() / qty;
        float validation = 1f * global.validation() / qty;
        float iterations = 1f * global.fuzzingIterations() / qty;
        float total = 1f * global.duration() / qty;

        //Speed
      b.append(String.format("\tper test: fuzzing: %.2fms; validation: %.2fms; iterations: %.2f; testing: %.2fms; Total: %.2fms%n", fuzzing, validation, iterations, testing, total));

        double perSec = 1.0 * global.duration() / 1000;
        b.append(String.format("\t%.2f tests/sec.", qty / perSec));
        return b.toString();
    }
}
