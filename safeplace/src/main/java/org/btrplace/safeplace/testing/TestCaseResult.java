/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing;


import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.testing.verification.VerifierResult;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;

import java.util.stream.Collectors;

import static org.btrplace.safeplace.testing.Result.CRASH;
import static org.btrplace.safeplace.testing.Result.OVER_FILTERING;
import static org.btrplace.safeplace.testing.Result.SUCCESS;
import static org.btrplace.safeplace.testing.Result.UNDER_FILTERING;

/**
 * @author Fabien Hermenier
 */
public class TestCaseResult {

    private Metrics metrics;

  private final TestCase tc;

    private SolvingStatistics stats;

  private final Result res;

  private final VerifierResult verifier;

    private Exception ex;
    public TestCaseResult(TestCase tc, SolvingStatistics stats, VerifierResult verify) {
        this.tc = tc;
        this.verifier = verify;
        this.res = makeResult(stats, verify);
        this.stats = stats;
        this.metrics = new Metrics();
    }

    public TestCaseResult(TestCase tc, Exception ex, VerifierResult verify) {
        this.tc = tc;
        this.verifier = verify;
        this.res = makeResult(stats, verify);
        this.ex = ex;
        this.metrics = new Metrics();
    }

    public Result result() {
        return res;
    }

    public void metrics(Metrics m) {
        metrics = m;
    }

    public Metrics metrics() {
        return metrics;
    }

    public TestCase testCase() {
        return tc;
    }

    @Override
    public String toString() {
        return "Test case:\n" + tc + "\n" +
                "res: " + res.toString().toLowerCase() + "\n" +
                "impl: " + implResults() + "\n" +
                "\t" + tc.instance().getSatConstraints().stream().map(SatConstraint::toString).collect(Collectors.joining(",", "(", ")\n")) +
                "spec: " + verifier + "\n";
    }

    private String implResults() {
        if (ex != null) {
            return stackTraceToString(ex);
        }
        if (stats.getSolutions().isEmpty()) {
            if (stats.completed()) {
                return "no solution";
            }
            return "not enough time";
        }
        return "solution(s)";
    }

    public static Result makeResult(SolvingStatistics stats, VerifierResult res) {
        if (stats == null) {
            return CRASH;
        }
        ReconfigurationPlan last = stats.lastSolution();
        if (Boolean.TRUE.equals(res.getStatus())) {
            //There must be a solution
            if (last == null) {
                //but no in practice
                if (stats.completed()) {
                    return OVER_FILTERING;
                }
                return CRASH;
            }
            return SUCCESS;
        } else if (Boolean.FALSE.equals(res.getStatus())) {
            if (last != null) {
                return UNDER_FILTERING;
            }
            return SUCCESS;
        }
        return CRASH;
    }

    public String stackTraceToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
