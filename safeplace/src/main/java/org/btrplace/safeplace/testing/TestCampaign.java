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

package org.btrplace.safeplace.testing;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.testing.fuzzer.DefaultReconfigurationPlanFuzzer;
import org.btrplace.safeplace.testing.fuzzer.DefaultTestCaseFuzzer;
import org.btrplace.safeplace.testing.fuzzer.TestCaseFuzzer;
import org.btrplace.safeplace.testing.limit.RunnerLimit;
import org.btrplace.safeplace.testing.reporting.DefaultReporting;
import org.btrplace.safeplace.testing.reporting.Reporting;
import org.btrplace.safeplace.testing.verification.Verifier;
import org.btrplace.safeplace.testing.verification.VerifierResult;
import org.btrplace.safeplace.testing.verification.btrplace.CSchedule;
import org.btrplace.safeplace.testing.verification.btrplace.Schedule;
import org.btrplace.safeplace.testing.verification.spec.SpecVerifier;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.DefaultParameters;
import org.btrplace.scheduler.choco.Parameters;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class TestCampaign implements Tester {

    private RunnerLimit limits;

    private Parameters params;

    private Verifier oracle;

    private TestCaseFuzzer tcFuzzer;

    private Reporting reporting;

    private List<Constraint> cstrs;

    private List<Constraint> cores;

    private Writer writer;

    public TestCampaign(List<Constraint> cstrs)  {
        tcFuzzer = new DefaultTestCaseFuzzer(new DefaultReconfigurationPlanFuzzer());
        limits = new RunnerLimit();
        this.cstrs = cstrs;
        cores = cstrs.stream().filter(c -> c.args().isEmpty()).collect(Collectors.toList());
        params = new DefaultParameters();
        params.getMapper().mapConstraint(Schedule.class, CSchedule.class);
        oracle = new SpecVerifier();
        reporting = new DefaultReporting();
    }

    public TestCampaign schedulerParams(Parameters ps) {
        params = ps;
        return this;
    }

    public Parameters schedulerParams() {
        return params;
    }

    public TestCampaign verifyWith(Verifier v) {
        oracle = v;
        return this;
    }

    public Verifier verifyWith() {
        return oracle;
    }

    public RunnerLimit limits() {
        return limits;
    }

    public int go() {
        TestCaseResult res;
        try {
            store("[\n");
            boolean first = true;
            do {
                if (!first) {
                    store(",\n");
                }
                TestCase tc = tcFuzzer.get();
                if (tc == null) {
                    //We are done
                    break;
                }
                if (first) {
                    reporting.start(tc.constraint());
                    first = false;
                }
                try {
                    if (writer != null) {
                        String json = tc.toJSON();
                        store(json + "\n");
                    }
                } catch (JSONConverterException ex) {
                    throw new IllegalArgumentException(ex);
                }
                long d = -System.currentTimeMillis();
                res = test(tc);
                d += System.currentTimeMillis();
                res.metrics().testing(d);
                res.metrics().validation(tcFuzzer.lastValidationDuration());
                // - validation because it is embedded
                res.metrics().fuzzing(Math.max(0, tcFuzzer.lastFuzzingDuration() - res.metrics().validation()));
                res.metrics().fuzzingIterations(tcFuzzer.lastFuzzingIterations());

                reporting.with(res);
            } while (limits.test(res));

            return reporting.done();
        } finally {
            store("]\n");
        }
    }

    private void store(String s) {
        if (writer == null) {
            return;
        }
        try {
            writer.write(s);
            writer.flush();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Reporting reporting() {
        return reporting;
    }

    public TestCampaign reporting(Reporting r) {
        reporting = r;
        return this;
    }

    public TestCampaign constraint(String c) {
        String lower = c.toLowerCase();
        for (Constraint cstr : cstrs) {
            if (lower.equalsIgnoreCase(cstr.id())) {
                fuzz().constraint(cstr);
                if (!cstr.args().isEmpty()) {
                    cores.forEach(x -> fuzz().validating(x, this));
                } else {
                    //Every other core constraints
                    cores.stream().filter(x -> !x.id().equals(cstr.id())).forEach(x -> fuzz().validating(x, this));
                }
                return this;
            }
        }
        throw new IllegalArgumentException("No specification for " + c);
    }

    public List<Constraint> constraints() {
        return cstrs;
    }

    public TestCaseFuzzer fuzz() {
        return tcFuzzer;
    }

    public TestCampaign fuzzer(TestCaseFuzzer f) {
        tcFuzzer = f;
        tcFuzzer.supportedConstraints(cstrs);
        return this;
    }

    public TestCampaign save(Writer w) {
        writer = w;
        return this;
    }

    public TestCampaign save(String path)  {
        try {
            return save(Files.newBufferedWriter(Paths.get(path), StandardOpenOption.TRUNCATE_EXISTING , StandardOpenOption.CREATE));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public TestCaseResult test(TestCase tc) {
        VerifierResult res = oracle.verify(tc);
        DefaultChocoScheduler sched = new DefaultChocoScheduler(params);
        try {
            ReconfigurationPlan plan = sched.solve(tc.instance());
            checkConsistency(plan, tc);
        } catch (RuntimeException e) {
            //A runtime exception is a failure. Should not happen
            return new TestCaseResult(tc, e, res);
        }
        return new TestCaseResult(tc, sched.getStatistics(), res);
    }

    private void checkConsistency(ReconfigurationPlan got, TestCase tc) {
        if (got != null && !tc.plan().equals(got)) {
            String output = "--- Instance"
                    + tc.instance().getSatConstraints().stream().map(SatConstraint::toString).collect(Collectors.joining("\n\t", "\t", ""))
                    + " ---\n";
            output += "Bad resulting plan. Expected:\n" + tc.plan().getOrigin().getViews() + "\n" + tc.plan() + "\nGot:\n" + got.getOrigin().getViews() + "\n" + got;
            throw new IllegalStateException(output);
        }
    }
}
