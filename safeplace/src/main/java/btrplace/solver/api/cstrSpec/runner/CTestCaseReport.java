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

package btrplace.solver.api.cstrSpec.runner;

import btrplace.solver.api.cstrSpec.CTestCaseResult;

/**
 * @author Fabien Hermenier
 */
public class CTestCaseReport {

    private String id;

    private int ok = 0, fp = 0, fn = 0;

    private Exception ex;

    private long duration;

    public CTestCaseReport(String id) {
        this.id = id;
    }

    public void add(CTestCaseResult r) {
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
        }
    }

    public void report(Exception e) {
        this.ex = e;
    }

    public Exception report() {
        return ex;
    }

    public void duration(long d) {
        duration = d;
    }

    public long duration() {
        return duration;
    }

    public String pretty() {
        if (ex != null) {
            return id + ": " + ex.getMessage();
        }
        return id + ": " + (ok + fn + fp) + " test(s); " + fp + " F/P; " + fn + " F/N (" + duration + "ms)";
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
}
