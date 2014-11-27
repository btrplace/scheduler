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

package org.btrplace.safeplace;

import org.btrplace.model.view.ModelView;
import org.btrplace.safeplace.verification.CheckerResult;

import static org.btrplace.safeplace.CTestCaseResult.Result.failure;
import static org.btrplace.safeplace.CTestCaseResult.Result.success;

/**
 * @author Fabien Hermenier
 */
public class CTestCaseResult {

    private CTestCaseMetrology metrics;
    private CTestCase reduced;

    public void setMetrics(CTestCaseMetrology metrics) {
        this.metrics = metrics;
    }

    public CTestCaseMetrology getMetrics() {
        return metrics;
    }

    public void setReduced(CTestCase reduced) {
        this.reduced = reduced;
    }

    public CTestCase getReduced() {
        return reduced;
    }

    public static enum Result {success, falsePositive, falseNegative, failure}

    private Result res;

    private String stdout;

    private String stderr;

    private CheckerResult res1, res2;

    private CTestCase tc;

    private long fuzzingDuration;

    public CTestCaseResult(CTestCase tc, CheckerResult r1, CheckerResult r2) {
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
            return CTestCaseResult.Result.falseNegative;
        }
        return CTestCaseResult.Result.falsePositive;
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
        CTestCase tc = reduced != null ? reduced : this.tc;
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

    public CTestCase getTestCase() {
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
