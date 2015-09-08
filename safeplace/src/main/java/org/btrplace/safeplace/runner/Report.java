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

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Report {

    private String id;

    private int ok = 0, fp = 0, fn = 0, fe = 0;

    private Exception ex;

    private Metrics metrics;

    public Report(String testName) {
        this.id = testName;
        metrics = new Metrics();
    }

    public String testName() {
        return id;
    }

    public void add(List<TestCaseResult> list) {
        for (TestCaseResult r : list) {
            add(r);
        }
    }

    public void add(TestCaseResult r) {
        switch (r.result()) {
            case success:
                ok++;
                break;
            case falseNegative:
                fn++;
                break;
            case falsePositive:
                fp++;
                break;
            case failure:
                fe++;
                break;
        }
        metrics = Metrics.sum(metrics, r.metrics());
    }

    public Metrics metrics() {
        return metrics;
    }

    public void report(Exception e) {
        this.ex = e;
    }

    public Exception report() {
        return ex;
    }


    public int ok() {
        return ok;
    }

    public int fp() {
        return fp;
    }

    public int fn() {
        return fn;
    }

    public int fe() {
        return fe;
    }

    public String pretty() {
        return testName() + " " + (ok + fp + fn + fe) + " tests: " + fp + "fp; " + fn + " fn; " + fe + " ex; (" + metrics.duration() + " ms)";
    }
}
