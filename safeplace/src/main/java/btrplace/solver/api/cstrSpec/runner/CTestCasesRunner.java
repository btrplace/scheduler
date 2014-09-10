package btrplace.solver.api.cstrSpec.runner;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.*;
import btrplace.solver.api.cstrSpec.fuzzer.ConstraintInputFuzzer;
import btrplace.solver.api.cstrSpec.guard.ErrorGuard;
import btrplace.solver.api.cstrSpec.guard.Guard;
import btrplace.solver.api.cstrSpec.guard.MaxTestsGuard;
import btrplace.solver.api.cstrSpec.guard.TimeGuard;
import btrplace.solver.api.cstrSpec.reducer.ElementsReducer;
import btrplace.solver.api.cstrSpec.reducer.PlanReducer;
import btrplace.solver.api.cstrSpec.reducer.Reducer;
import btrplace.solver.api.cstrSpec.reducer.SignatureReducer2;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.btrplace.CheckerVerifier;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

    private ReductionStatistics reductionStatistics = new ReductionStatistics();

    private int nb;

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

    public CTestCasesRunner(Class testClass, String testName, String cstr) throws Exception {
        this.testClass = testClass;
        this.testName = testName;
        id = testClass.getSimpleName() + "." + testName;
        nb = 0;
        guards = new ArrayList<>();
        timeout(10);
        maxTests(1000);
        verifier = new ImplVerifier();
        specVerifier = new SpecVerifier();
        SpecReader r = new SpecReader();
        //TODO Groumph, hardcoded
        Specification spec = r.getSpecification(new File("src/main/cspec/v1.cspec"));
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

    private void save(CTestCase tc) {

    }

    private void save(CTestCase tc, CTestCaseResult r) {
        if (r.result() != CTestCaseResult.Result.success) {
            CTestCase tc2 = reduce(r.result(), tc, continuous);
            CTestCaseResult res2 = test(tc2);
            if (res2.result() != r.result()) {
                System.err.println(tc2.getPlan().equals(tc.getPlan()));
                System.err.println(tc2.getParameters().equals(tc.getParameters()));
                System.err.println(tc.getParameters() + " " + tc2.getParameters());
                System.err.println(tc2.getConstraint().equals(tc.getConstraint()));
                throw new RuntimeException("Failure in the reduction.\nWas:\n" + tc + "\nwith\n" + r + "\nNow:\n" + tc2 + "\nwith\n" + res2);
            }
            //System.out.println(res2);
        }
    }

    private boolean checkPre(ReconfigurationPlan p) {

        //Necessarily against the continuous version
        for (Constraint c : pre) {
            CheckerResult res = specVerifier.verify(c, Collections.<Constant>emptyList(), p);
            if (!res.getStatus()) {
                return false;
            }
        }
        for (Constraint c : pre) {
            CheckerResult res = verifier.verify(c, Collections.<Constant>emptyList(), p);
            if (!res.getStatus()) {
                return false;
            }
        }
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
        //System.err.println(tc.getConstraint().toString(tc.getParameters()));
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

        ReconfigurationPlan p;
        do {
            p = in.next();
        } while (!checkPre(p));

        List<Constant> args = cig.newParams();
        CTestCase tc = new CTestCase(testClass, testName, nb++, cstr, args, p, continuous);
        save(tc);
        CTestCaseResult res = test(tc);
        save(tc, res);
        return res;
    }

    @Override
    public Iterator<CTestCaseResult> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        if (ex != null) {
            return false;
        }

        if (!in.hasNext()) {
            return false;
        }

        if (continuous && cstr.isDiscrete()) {
            ex = new UnsupportedOperationException(cstr.id() + " only supports discrete restriction");
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
            return false;
        }
        if (prev == null) {
            return true;
        }
        for (Guard g : guards) {
            if (!g.accept(prev)) {
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
        CTestCase x = tc;
        try {
            for (Reducer r : reducers) {
                x = r.reduce(x, specVerifier, verifier, errType);
                CTestCaseResult res = test(x);
                if (res.result() != errType) {
                    throw new RuntimeException("The error type changed from " + errType + " to " + res.result());
                }
            }
            reductionStatistics.report(tc, x);
        } catch (Exception e) {
            e.printStackTrace();
            return x;
        }
        return x;
    }
}
