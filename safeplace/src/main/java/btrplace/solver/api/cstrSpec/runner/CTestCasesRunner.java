package btrplace.solver.api.cstrSpec.runner;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.CTestCase;
import btrplace.solver.api.cstrSpec.CTestCaseResult;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.guard.ErrorGuard;
import btrplace.solver.api.cstrSpec.guard.Guard;
import btrplace.solver.api.cstrSpec.guard.MaxTestsGuard;
import btrplace.solver.api.cstrSpec.guard.TimeGuard;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;

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

    private boolean repair = false;

    private Verifier verifier;

    private String id;

    private SpecVerifier specVerifier;

    private int nb = 1;

    private CTestCaseResult prev;

    private Exception ex;

    private Iterator<CTestCase> in = new Iterator<CTestCase>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public CTestCase next() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };

    private List<Guard> guards;

            /*return v.stopAfter(X)
                .maxTests(Y)
                .maxUniqueTests(Y)
                .maxFailure(Z)
                .continuous()
                .discrete()
                .repair
                .rebuild
          */

    public CTestCasesRunner(String id) {
        this.id = id;
        guards = new ArrayList<>();
        timeout(10);
        maxTests(1000);
        verifier = new ImplVerifier();
        specVerifier = new SpecVerifier();
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

    public CTestCasesRunner repair() {
        repair = true;
        return this;
    }

    public CTestCasesRunner rebuild() {
        repair = false;
        return this;
    }

    public CTestCasesRunner setIn(Iterator<CTestCase> in) {
        this.in = in;
        return this;
    }

    public CTestCasesRunner setId(String i) {
        this.id = i;
        return this;
    }

    private void save(CTestCase tc) {
        //System.out.println("Save " + tc.id() + " into " + id + "_testcase.json");
    }

    private void save(CTestCaseResult r) {
        //System.out.println("Save " + r.id() + " into " + id + "_result.json");
    }

    private boolean checkPre(CTestCase tc) {
        ReconfigurationPlan p = tc.getPlan();
        List<Constraint> pre = tc.getPre();
        SpecVerifier spec = new SpecVerifier();
        for (Constraint c : pre) {
            CheckerResult res = spec.verify(c, p, Collections.<Constant>emptyList());
            if (!res.getStatus()) {
                return false;
            }
        }
        for (Constraint c : pre) {
            CheckerResult res = verifier.verify(c, p, Collections.<Constant>emptyList());
            if (!res.getStatus()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public CTestCaseResult next() {

        CTestCase tc;
        do {
            tc = in.next();
        } while (!checkPre(tc));

        save(tc);
        CheckerResult specRes;
        CheckerResult res;

        PrintStream oldOut = System.out;
        PrintStream olderr = System.err;
        try {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            ByteArrayOutputStream bErr = new ByteArrayOutputStream();
            TeePrintStream teeOut = new TeePrintStream(bOut, System.out);
            TeePrintStream teeErr = new TeePrintStream(bErr, System.err);

            if (continuous) {
                specRes = specVerifier.verify(tc.getConstraint(), tc.getPlan(), tc.getParameters());
                System.setOut(teeOut);
                System.setErr(teeErr);
                res = verifier.verify(tc.getConstraint(), tc.getPlan(), tc.getParameters());
            } else {
                Model src = tc.getPlan().getOrigin();
                Model dst = tc.getPlan().getResult();
                specRes = specVerifier.verify(tc.getConstraint(), src, dst, tc.getParameters());
                System.setOut(teeOut);
                System.setErr(teeErr);
                res = verifier.verify(tc.getConstraint(), src, dst, tc.getParameters());
            }

            CTestCaseResult.Result r;
            if (specRes.getStatus().equals(res.getStatus())) {
                r = CTestCaseResult.Result.success;
            } else {
                if (specRes.getStatus()) {
                    r = CTestCaseResult.Result.falseNegative;
                } else {
                    r = CTestCaseResult.Result.falsePositive;
                }
            }
            prev = new CTestCaseResult(id + "_" + (nb++), tc, r);
            prev.setStdout(bOut.toString());
            prev.setStderr(bOut.toString());
            save(prev);
            return prev;
        } finally {
            System.setOut(oldOut);
            System.setErr(olderr);
        }
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
}
