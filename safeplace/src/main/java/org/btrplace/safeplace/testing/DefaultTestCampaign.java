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

package org.btrplace.safeplace.testing;

import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.testing.fuzzer.ConfigurableFuzzer;
import org.btrplace.safeplace.testing.fuzzer.DefaultFuzzer;
import org.btrplace.safeplace.testing.fuzzer.Fuzzer;
import org.btrplace.safeplace.testing.fuzzer.Replay;
import org.btrplace.safeplace.testing.limit.Limits;
import org.btrplace.safeplace.testing.reporting.Counting;
import org.btrplace.safeplace.testing.reporting.Report;
import org.btrplace.safeplace.testing.verification.Verifier;
import org.btrplace.safeplace.testing.verification.VerifierResult;
import org.btrplace.safeplace.testing.verification.btrplace.CSchedule;
import org.btrplace.safeplace.testing.verification.btrplace.Schedule;
import org.btrplace.safeplace.testing.verification.spec.SpecVerifier;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.DefaultParameters;
import org.btrplace.scheduler.choco.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class DefaultTestCampaign implements Tester, TestCampaign {

    private Limits limits;

    private Parameters params;

    private Verifier oracle;

    private Fuzzer tcFuzzer;

    private Report report;

    private List<Constraint> cstrs;

    private List<Constraint> cores;

    private boolean printProgress;

    private Consumer<TestCaseResult> defectHook = DefectHooks.failedAssertion;

    private String name;

    public DefaultTestCampaign(String name, List<Constraint> cstrs) {
        this.name = name;
        limits = new Limits();
        this.cstrs = cstrs;
        cores = cstrs.stream().filter(c -> c.args().isEmpty()).collect(Collectors.toList());
        params = new DefaultParameters();
        params.getMapper().mapConstraint(Schedule.class, CSchedule.class);
        oracle = new SpecVerifier();
        report = new Counting();
    }

    @Override
    public TestCampaign schedulerParams(Parameters ps) {
        params = ps;
        return this;
    }

    @Override
    public Parameters schedulerParams() {
        return params;
    }

    @Override
    public TestCampaign verifyWith(Verifier v) {
        oracle = v;
        return this;
    }

    @Override
    public Limits limits() {
        return limits;
    }

    @Override
    @SuppressWarnings("squid:S106")
    public Report go() {
        TestCaseResult res;
        int nb = 1;
        boolean first = true;
        do {
            TestCase tc = tcFuzzer.get();
            if (tc == null) {
                //We are done
                break;
            }
            if (first) {
                System.out.println(this.name + ": " + tc.constraint().signatureToString());
                first = false;
            }
            long d = -System.currentTimeMillis();
            res = test(tc);
            d += System.currentTimeMillis();
            if (res.result() != Result.SUCCESS) {
                defectHook.accept(res);
            }
            printProgress(res.result(), nb);

            res.metrics().testing(d);
            res.metrics().validation(tcFuzzer.lastValidationDuration());
            // - validation because it is embedded
            res.metrics().fuzzing(Math.max(0, tcFuzzer.lastFuzzingDuration() - res.metrics().validation()));
            res.metrics().fuzzingIterations(tcFuzzer.lastFuzzingIterations());
            report.with(res);
            nb++;
        } while (limits.test(res.result()));
        if (printProgress && nb % 80 != 0) {
            System.out.println();
        }
        return report;
    }

    @SuppressWarnings("squid:S106")
    private void printProgress(Result res, int nb) {
        if (!printProgress) {
            return;
        }
        switch (res) {
            case UNDER_FILTERING:
                System.out.print("-");
                break;
            case OVER_FILTERING:
                System.out.print("+");
                break;
            case CRASH:
                System.out.print("x");
                break;
            default:
                System.out.print(".");
                break;
        }
        if (nb % 80 == 0) {
            System.out.println();
        }
    }

    @Override
    public TestCampaign reportTo(Report r) {
        report = r;
        return this;
    }

    @Override
    public ConfigurableFuzzer check(String c) {
        String lower = c.toLowerCase();

        Optional<Constraint> cstr = cstrs.stream().filter(x -> lower.equalsIgnoreCase(x.id())).findFirst();
        if (!cstr.isPresent()) {
            throw new IllegalArgumentException("No specification for constraint '" + c + "'");
        }

        List<Constraint> pre = cores.stream().filter(x -> !lower.equalsIgnoreCase(x.id())).collect(Collectors.toList());
        ConfigurableFuzzer f = new DefaultFuzzer(this, cstr.get(), pre);
        tcFuzzer = f;
        return f;
    }

    @Override
    public TestCaseResult test(TestCase tc) {
        VerifierResult res = oracle.verify(tc);
        DefaultChocoScheduler sched = new DefaultChocoScheduler(params);
        try {
            ReconfigurationPlan plan = sched.solve(tc.instance());
            checkConsistency(plan, tc);
        } catch (RuntimeException e) {
            //A runtime exception is a CRASH. Should not happen
            return new TestCaseResult(tc, e, res);
        }
        return new TestCaseResult(tc, sched.getStatistics(), res);
    }

    @Override
    public TestCampaign printProgress(boolean b) {
        printProgress = b;
        return this;
    }

    private void checkConsistency(ReconfigurationPlan got, TestCase tc) {
        if (got != null && !tc.plan().equals(got)) {
            String output = "--- Instance"
                    + tc.instance().getSatConstraints()
                    .stream()
                    .map(SatConstraint::toString)
                    .collect(Collectors.joining("\n\t", "\t", ""))
                    + " ---\n";
            output += "Bad resulting plan. Expected:\n" + tc.plan().getOrigin().getViews() + "\n" + tc.plan() + "\nGot:\n" + got.getOrigin().getViews() + "\n" + got;
            throw new IllegalStateException(output);
        }
    }

    @Override
    public TestCampaign onDefect(Consumer<TestCaseResult> res) {
        this.defectHook = res;
        return this;
    }

    @Override
    public TestCampaign replay(Path p) {
        try {
            this.tcFuzzer = new Replay(cstrs, p);
            return this;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
