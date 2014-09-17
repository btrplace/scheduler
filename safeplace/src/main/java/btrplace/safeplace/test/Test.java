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

package btrplace.safeplace.test;

import btrplace.safeplace.CTestCaseMetrology;
import btrplace.safeplace.CTestCaseResult;
import btrplace.safeplace.runner.CTestCaseReport;
import btrplace.safeplace.runner.CTestCasesRunner;
import btrplace.safeplace.runner.TestsScanner;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Test {

    private static int verbosity = 2;

    public static boolean raw = true;

    public static void main(String[] args) {
        TestsScanner scanner = null;
        try {
            scanner = new TestsScanner();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        if (args.length == 0) {
            scanner.restrictToTest("Bench");
        } else {
            scanner.restrictToGroup(args[0]);
        }

        List<CTestCasesRunner> runners = null;
        try {
            runners = scanner.scan();
        } catch (Exception e) {
        }

        if (runners.isEmpty()) {
            System.out.println("No tests found");
            System.exit(0);
        }
        for (CTestCasesRunner runner : runners) {
            CTestCaseReport report = new CTestCaseReport(runner.id());
            for (CTestCaseResult res : runner) {
                if (raw) {
                    CTestCaseMetrology metrics = res.getMetrics();
                    System.out.println(metrics);
                }
                report.add(res);
            }
            report.duration(runner.getDuration());
            report.report(runner.report());
            if (report.report() != null || report.fn() > 0 || report.fp() > 0 || verbosity > 1) {
                System.err.println(report.pretty());
            }
        }
    }
}