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

import org.btrplace.model.Model;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.*;
import org.btrplace.safeplace.fuzzer.ConstraintInputFuzzer;
import org.btrplace.safeplace.guard.ErrorGuard;
import org.btrplace.safeplace.guard.Guard;
import org.btrplace.safeplace.guard.MaxTestsGuard;
import org.btrplace.safeplace.guard.TimeGuard;
import org.btrplace.safeplace.reducer.ElementsReducer;
import org.btrplace.safeplace.reducer.PlanReducer;
import org.btrplace.safeplace.reducer.Reducer;
import org.btrplace.safeplace.reducer.SignatureReducer2;
import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.verification.CheckerResult;
import org.btrplace.safeplace.verification.Verifier;
import org.btrplace.safeplace.verification.btrplace.CheckerVerifier;
import org.btrplace.safeplace.verification.btrplace.ImplVerifier;
import org.btrplace.safeplace.verification.spec.SpecModel;
import org.btrplace.safeplace.verification.spec.SpecVerifier;
import org.btrplace.safeplace.verification.spec.VerifDomain;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class CTestCasesRunner implements Iterator<CTestCaseResult>, Iterable<CTestCaseResult> {

    private boolean continuous = true;

    private Verifier verifier;

    private String id;

    private Class testClass;

    private String testName;

    private SpecVerifier specVerifier;

    private CTestCaseResult prev;

    private Exception ex;

    private Constraint cstr;

    private List<Constraint> pre;

    private ConstraintInputFuzzer cig;

    private List<VerifDomain> doms;

    private List<Reducer> reducers;

    private int nb;

    private boolean validate;

    private long preconditionCheckDuration;

    private long begin = -1, end = -1;

    private CTestCaseMetrology metrics;

    private Iterator<ReconfigurationPlan> in = new Iterator<ReconfigurationPlan>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public ReconfigurationPlan next() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };

    private List<Guard> guards;

    public CTestCasesRunner(Specification spec, Class testClass, String testName, String cstr) throws Exception {
        this.testClass = testClass;
        this.testName = testName;
        id = testClass.getSimpleName() + "." + testName;
        nb = 0;
        guards = new ArrayList<>();
        timeout(10);
        maxTests(1000);
        verifier = new ImplVerifier();
        specVerifier = new SpecVerifier();
        this.cstr = spec.get(cstr);
        if (this.cstr == null) {
            ex = new Exception("Spec for constraint '" + cstr + "' not found");
        }
        pre = makePreconditions(this.cstr, spec);
        doms = new ArrayList<>();
        reducers = new ArrayList<>();
        reducers.add(new SignatureReducer2());
        reducers.add(new PlanReducer());
        reducers.add(new ElementsReducer());
    }

    public CTestCasesRunner clearReducers() {
        reducers.clear();
        return this;
    }

    public CTestCasesRunner guard(Guard g) {
        for (Iterator<Guard> ite = guards.iterator(); ite.hasNext(); ) {
            Guard gg = ite.next();
            if (gg.getClass().equals(g.getClass())) {
                ite.remove();
                break;
            }
        }
        guards.add(g);
        return this;
    }

    public CTestCasesRunner verifier(Verifier v) {
        verifier = v;
        return this;
    }

    public CTestCasesRunner maxTests(int m) {
        return guard(new MaxTestsGuard(m));
    }

    public CTestCasesRunner maxFailures(int m) {
        return guard(new ErrorGuard(m));
    }

    public CTestCasesRunner timeout(int s) {
        return guard(new TimeGuard(s));
    }

    public CTestCasesRunner continuous() {
        continuous = true;
        return this;
    }

    public CTestCasesRunner discrete() {
        continuous = false;
        return this;
    }

    public CTestCasesRunner setIn(Iterator<ReconfigurationPlan> in) {
        this.in = in;
        return this;
    }

    public CTestCasesRunner setId(String i) {
        this.id = i;
        return this;
    }

    public CTestCasesRunner validate(boolean b) {
        this.validate = b;
        return this;
    }

    private void save(CTestCase tc) {

    }

    private boolean checkPre(ReconfigurationPlan p) {
        preconditionCheckDuration -= System.currentTimeMillis();
        if (validate) {
            //Necessarily against the continuous version
            for (Constraint c : pre) {
                CheckerResult res = specVerifier.verify(c, Collections.<Constant>emptyList(), p);
                if (!res.getStatus()) {
                    preconditionCheckDuration += System.currentTimeMillis();
                    return false;
                }
            }
            for (Constraint c : pre) {
                CheckerResult res = verifier.verify(c, Collections.<Constant>emptyList(), p);
                if (!res.getStatus()) {
                    preconditionCheckDuration += System.currentTimeMillis();
                    return false;
                }
            }
        }
        preconditionCheckDuration += System.currentTimeMillis();
        return true;
    }

    private List<Constraint> makePreconditions(Constraint c, Specification spec) {
        List<Constraint> pre = new ArrayList<>();
        for (Constraint x : spec.getConstraints()) {
            if (x.isCore()) {
                pre.add(x);
            }
        }
        pre.remove(c);
        return pre;
    }

    private CTestCaseResult test(CTestCase tc) {
        CheckerResult specRes;
        CheckerResult res;
        PrintStream oldOut = System.out;
        PrintStream olderr = System.err;
        try {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            ByteArrayOutputStream bErr = new ByteArrayOutputStream();
            TeePrintStream teeOut = new TeePrintStream(bOut, System.out);
            TeePrintStream teeErr = new TeePrintStream(bErr, System.err);

            System.setOut(teeOut);
            System.setErr(teeErr);

            if (tc.continuous()) {
                specRes = specVerifier.verify(tc.getConstraint(), tc.getParameters(), tc.getPlan());
                res = verifier.verify(tc.getConstraint(), tc.getParameters(), tc.getPlan());
            } else {
                Model src = tc.getPlan().getOrigin();
                Model dst = tc.getPlan().getResult();
                specRes = specVerifier.verify(tc.getConstraint(), tc.getParameters(), src, dst);
                res = verifier.verify(tc.getConstraint(), tc.getParameters(), src, dst);
            }

            prev = new CTestCaseResult(tc, specRes, res);
            prev.setStdout(bOut.toString());
            prev.setStderr(bOut.toString());
            return prev;
        } finally {
            System.setOut(oldOut);
            System.setErr(olderr);
        }
    }

    @Override
    public CTestCaseResult next() {

        if (begin < 0) {
            begin = System.currentTimeMillis();
        }
        long lastFuzzingDuration = 0;
        ReconfigurationPlan p;
        preconditionCheckDuration = 0;

        do {
            lastFuzzingDuration -= System.currentTimeMillis();
            p = in.next();
            lastFuzzingDuration += System.currentTimeMillis();
        } while (!checkPre(p));

        lastFuzzingDuration -= System.currentTimeMillis();
        List<Constant> args = cig.newParams();
        lastFuzzingDuration += System.currentTimeMillis();


        CTestCase tc = new CTestCase(testClass, testName, nb++, cstr, args, p, continuous);
        save(tc);

        long lastTestDuration = -System.currentTimeMillis();
        CTestCaseResult res = test(tc);
        lastTestDuration += System.currentTimeMillis();


        metrics = new CTestCaseMetrology(tc, verifier, res.result());
        metrics.setFuzzingDuration(lastFuzzingDuration);
        metrics.setTestingDuration(lastTestDuration);
        metrics.setValidationDuration(preconditionCheckDuration);


        long lastReduceDuration = -System.currentTimeMillis();
        CTestCase tc2 = reduce(res.result(), tc, continuous);
        lastReduceDuration += System.currentTimeMillis();
        metrics.setReduceDuration(lastReduceDuration);
        metrics.setReduced(tc2);
        res.setReduced(tc2);
        save(tc2);
        res.setMetrics(metrics);
        return res;
    }

    @Override
    public Iterator<CTestCaseResult> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        if (ex != null) {
            end = System.currentTimeMillis();
            return false;
        }

        if (!in.hasNext()) {
            end = System.currentTimeMillis();
            return false;
        }

        if (continuous && cstr.isDiscrete()) {
            ex = new UnsupportedOperationException(cstr.id() + " only supports discrete restriction");
            end = System.currentTimeMillis();
            return false;
        }

        if (cig == null) {
            ReconfigurationPlan p = in.next();
            SpecModel mo = new SpecModel(p.getOrigin());
            for (VerifDomain dom : doms) {
                mo.add(dom);
            }
            try {
                cig = new ConstraintInputFuzzer(this.cstr, mo);
            } catch (UnsupportedOperationException e) {
                ex = e;
            }
        }

        if (ex != null) {
            end = System.currentTimeMillis();
            return false;
        }
        if (prev == null) {
            return true;
        }
        for (Guard g : guards) {
            if (!g.accept(prev)) {
                end = System.currentTimeMillis();
                return false;
            }
        }
        return true;
    }

    public void report(Exception e) {
        this.ex = e;
    }

    public Exception report() {
        return ex;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public String id() {
        return id;
    }

    public CTestCasesRunner dom(VerifDomain v) {
        for (Iterator<VerifDomain> ite = doms.iterator(); ite.hasNext(); ) {
            VerifDomain vv = ite.next();
            if (vv.type().equals(v.type())) {
                ite.remove();
                break;
            }
        }
        doms.add(v);
        return this;
    }

    public CTestCasesRunner reduceWith(Reducer r) {
        for (Iterator<Reducer> ite = reducers.iterator(); ite.hasNext(); ) {
            Reducer vv = ite.next();
            if (vv.getClass() == r.getClass()) {
                ite.remove();
                break;
            }
        }
        reducers.add(r);
        return this;
    }

    public ImplVerifier impl() {
        ImplVerifier i = new ImplVerifier();
        verifier = i;
        return i;
    }

    public CheckerVerifier checker() {
        CheckerVerifier i = new CheckerVerifier();
        verifier = i;
        return i;
    }

    public CTestCasesRunner wipeReducers() {
        reducers.clear();
        return this;
    }

    private CTestCase reduce(CTestCaseResult.Result errType, CTestCase tc, boolean c) {
        if (errType == CTestCaseResult.Result.success) {
            return tc;
        }
        CTestCase x = tc;
        try {
            for (Reducer r : reducers) {
                x = r.reduce(x, specVerifier, verifier, errType);
                CTestCaseResult res = test(x);
                if (res.result() != errType) {
                    System.err.println("The error type changed from " + errType + " to " + res.result());
                    return tc;
                }
            }
            return x;
        } catch (Exception e) {
            return tc;
        }
    }


    public long getDuration() {
        return end - begin;
    }
}
