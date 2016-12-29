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

import com.google.common.io.Files;
import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.testing.Metrics;
import org.btrplace.safeplace.testing.TestCaseResult;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Fabien Hermenier
 */
@SuppressWarnings("squid:S106")
public class StoredReporting implements Reporting {

    private int i;

    private Metrics globalMetrics;

    private int fp;
    private int fn;
    private int failures;
    private int ok;

    private int verbosity;

    private Predicate<TestCaseResult> printPredicate = r -> false;

    private List<TestCaseResult> toPrint = new ArrayList<>();

    private Constraint cstr;

    private String root;

    public StoredReporting(String root) {
        this.root = root;
    }

    @Override
    public void start(Constraint cstr) {
        if (this.cstr != null) {
            save();
        }
        this.cstr = cstr;
        if (verbosity >= 1) {
            System.out.println(cstr.signatureToString() + ": ");
        }
        fp = 0;
        fn = 0;
        failures = 0;
        ok = 0;

    }

    private void save() {
        if (toPrint.isEmpty()) {
            return;
        }
        File f = Paths.get(root, cstr.id() + "_output.txt").toFile();
        try {
            java.nio.file.Files.deleteIfExists(f.toPath());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        for (TestCaseResult res : toPrint) {
            try {
                Files.append("--------------------------------------------------------\n" + res.toString(), f, Charset.defaultCharset());
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
        toPrint.clear();
    }

    @Override
    public void with(TestCaseResult r) {
        String op;
        switch (r.result()) {
            case falsePositive:
                op = "+";
                fp++;
                break;
            case falseNegative:
                op = "-";
                fn++;
                break;
            case failure:
                op = "x";
                failures++;
                break;
            default:
                op = ".";
                ok++;
                break;
        }
        if (verbosity > 1 && verbosity != 99) {
            System.out.print(op);
            if (++i % 80 == 0) {
                System.out.println();
            }
        }
        if (globalMetrics == null) {
            globalMetrics = r.metrics();
        } else {
            globalMetrics = globalMetrics.plus(r.metrics());
        }
        if (printPredicate.test(r)) {
                toPrint.add(r);
        }
        if (verbosity == 99) {
            System.out.println(r);
        }
    }

    @Override
    public int done() {
        if (i % 80 != 0 && verbosity > 1) {
            System.out.println();
        }
        if (verbosity >= 1 && globalMetrics != null) {
            int qty = ok + fp + fn + failures;
            double fuzzing = 1d * globalMetrics.fuzzing() / qty;
            double testing = 1d * globalMetrics.testing() / qty;
            double validation = 1d * globalMetrics.validation() / qty;
            double iterations = 1d * globalMetrics.fuzzingIterations() / qty;
            double total = 1d * globalMetrics.duration() / qty;
            System.out.println("\t" + ok + " Success; " + fp + " FP(s); " + fn + " FN(s); " + failures + " failures");

            System.out.println("\tPer test: fuzzing: " + fuzzing + " ms; validation: " + validation + " ms; iterations: " + iterations + "; testing: " + testing + " ms; Total: " + total + " ms");

            double perSec = 1.0 * globalMetrics.duration() / 1000;
            System.out.println("\t" + (int) (qty / perSec) + " tests/sec.");
        }

        save();
        return fp + fn + failures;
    }

    @Override
    public Reporting verbosity(int n) {
        verbosity = n;
        return this;
    }

    @Override
    public Reporting capture(Predicate<TestCaseResult> r) {
        printPredicate = r;
        return this;
    }
}
