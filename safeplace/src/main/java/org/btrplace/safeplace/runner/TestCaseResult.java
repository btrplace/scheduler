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

import org.btrplace.model.view.ModelView;
import org.btrplace.safeplace.fuzzer.TestCase;
import org.btrplace.safeplace.verification.CheckerResult;

import static org.btrplace.safeplace.runner.TestCaseResult.Result.failure;
import static org.btrplace.safeplace.runner.TestCaseResult.Result.success;

/**
 * @author Fabien Hermenier
 */
public class TestCaseResult {

    private Metrics metrics;
    private TestCase reduced;

    public void metrics(Metrics m) {
        this.metrics = m;
    }

    public Metrics metrics() {
        return metrics;
    }

    public void setReduced(TestCase reduced) {
        this.reduced = reduced;
    }

    public TestCase getReduced() {
        return reduced;
    }

    public static enum Result {success, falsePositive, falseNegative, failure}

    private Result res;

    private String stdout;

    private String stderr;

    private CheckerResult res1, res2;

    private TestCase tc;

    private long fuzzingDuration;

    public TestCaseResult(TestCase tc, CheckerResult r1, CheckerResult r2) {
        stdout = "";
        stderr = "";
        res1 = r1;
        res2 = r2;
        this.tc = tc;
        res = makeResult(res1, res2);
    }

    public static Result makeResult(CheckerResult res1, CheckerResult res2) {
        if (res2.getStatus() == null) {
            return failure;
        }
        if (res1.getStatus().equals(res2.getStatus())) {
            return success;
        }
        if (res1.getStatus()) {
            return TestCaseResult.Result.falseNegative;
        }
        return TestCaseResult.Result.falsePositive;
    }

    public void setStdout(String s) {
        stdout = s;
    }

    public void setStderr(String s) {
        stderr = s;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("id: ").append(tc.id()).append("\n");
        b.append("constraint: ").append(tc.getConstraint().toString(tc.getParameters())).append("\n");
        b.append("specRes: ").append(res1).append("\n");
        b.append("vRes: ").append(res2).append("\n");
        b.append("res: ").append(res).append("\n");
        TestCase tc = reduced != null ? reduced : this.tc;
        b.append("origin:\n").append(tc.getPlan().getOrigin().getMapping());
        if (!tc.getPlan().getOrigin().getViews().isEmpty()) {
            for (ModelView v : tc.getPlan().getOrigin().getViews()) {
                b.append("view " + v.getIdentifier() + ": " + v + "\n");
            }
        }
        b.append("actions:\n").append(tc.getPlan());
        return b.toString();
    }

    public Result result() {
        return res;
    }

    public TestCase getTestCase() {
        return tc;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public String errMessage() {
        switch (res) {
            case falsePositive:
                return "False positive: " + res1.toString();
            case falseNegative:
                return "False negative: " + res2.toString();
            case failure:
                return "Failure: " + res2.toString();
        }
        return "";
    }
}
