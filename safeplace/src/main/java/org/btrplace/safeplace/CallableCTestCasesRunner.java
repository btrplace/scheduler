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

import org.btrplace.safeplace.runner.CTestCaseReport;
import org.btrplace.safeplace.runner.CTestCasesRunner;

import java.util.concurrent.Callable;

/**
 * @author Fabien Hermenier
 */
public class CallableCTestCasesRunner implements Callable<CTestCaseReport> {

    private CTestCasesRunner runner;

    public CallableCTestCasesRunner(CTestCasesRunner r) {
        this.runner = r;
    }

    @Override
    public CTestCaseReport call() throws Exception {
        System.out.println("Started");
        CTestCaseReport report = new CTestCaseReport(runner.id());
        report.report(runner.report());
        for (CTestCaseResult res : runner) {
            report.add(res);
        }
        System.out.println("Finished");
        return report;
    }
}
