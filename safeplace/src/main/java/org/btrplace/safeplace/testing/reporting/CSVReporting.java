/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.testing.Result;
import org.btrplace.safeplace.testing.TestCaseResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Predicate;

/**
 * @author Fabien Hermenier
 */
@SuppressWarnings("squid:S106")
public class CSVReporting implements Reporting {

    private Path output;

    private String label;
    private static final String fmt = "%s;%s;%d;%d;%d;%d;%d;%d;%d;%s\n";

    private int verbose;

    private int failures;
    private int ok;
    private int fn;
    private int fp;
    public static final String HEADER = "constraint;label;continuous;vms;nodes;fuzzing;validation;iterations;testing;result\n";

    public CSVReporting(Path p, String label) {
        this.output = p;
        this.label = label;
        verbose = 0;

    }
    @Override
    public void start(Constraint cstr) {
        System.out.println(cstr.id());
        ok = 0;
        fn = 0;
        fp = 0;
        failures = 0;
    }

    @Override
    public Reporting verbosity(int n) {
        verbose = n;
        return this;
    }

    @Override
    public void with(TestCaseResult r) {
        try {
            if (output.getParent().toFile().exists()) {
                Files.createDirectories(output.getParent());
            }
            if (!output.toFile().exists()) {
                Files.write(output, HEADER.getBytes(), StandardOpenOption.CREATE);
            }
            //id;num;continuous;vms;nodes;fuzzing;validation;testing;result)
            String res = String.format(fmt,
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
            if (r.result() != Result.success) {
                if (r.result() == Result.falseNegative) {
                    fn++;
                } else if (r.result() == Result.falsePositive) {
                    fp++;
                } else {
                    failures++;
                }
            } else {
                ok++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int done() {
        if (verbose > 0) {
            System.out.println("\t" + ok + " Success; " + fp + " FP(s); " + fn + " FN(s); " + failures + " failures");
        }
        return failures + fp + fn;
    }

    @Override
    public Reporting capture(Predicate<TestCaseResult> r) {
        return this;
    }
}
