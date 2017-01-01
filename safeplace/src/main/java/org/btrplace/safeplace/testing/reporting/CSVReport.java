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

package org.btrplace.safeplace.testing.reporting;

import org.btrplace.safeplace.testing.TestCaseResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A report that store the test case results inside CSV file (one file per constraint).
 * @author Fabien Hermenier
 */
public class CSVReport extends Counting {

    private Path output;

    private String label;
    private static final String ROW_FORMAT = "%s;%s;%d;%d;%d;%d;%d;%d;%d;%s\n";

    /**
     * The default header.
     */
    public static final String HEADER = "constraint;label;CONTINUOUS;vms;nodes;fuzzing;validation;iterations;testing;result\n";

    /**
     * New report
     *
     * @param p     the path where to store the CSV files.
     * @param label the label for each constraint
     */
    public CSVReport(Path p, String label) {
        this.output = p;
        this.label = label;

    }

    @Override
    public void with(TestCaseResult r) {
        super.with(r); //to count
        try {
            if (output.getParent().toFile().exists()) {
                Files.createDirectories(output.getParent());
            }
            if (!output.toFile().exists()) {
                Files.write(output, HEADER.getBytes(), StandardOpenOption.CREATE);
            }
            //id;num;CONTINUOUS;vms;nodes;fuzzing;validation;testing;result)
            String res = String.format(ROW_FORMAT,
                    r.testCase().constraint().id(),
                    label,
                    r.testCase().continuous() ? 1 : 0,
                    r.testCase().instance().getModel().getMapping().getNbVMs(),
                    r.testCase().instance().getModel().getMapping().getNbNodes(),
                    r.metrics().fuzzing(),
                    r.metrics().validation(),
                    r.metrics().fuzzingIterations(),
                    r.metrics().testing(),
                    r.result().toString());

            Files.write(output, res.getBytes(), StandardOpenOption.WRITE,StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
