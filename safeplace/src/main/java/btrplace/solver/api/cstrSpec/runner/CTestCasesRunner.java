package btrplace.solver.api.cstrSpec.runner;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.CTestCase;
import btrplace.solver.api.cstrSpec.CTestCaseResult;
import btrplace.solver.api.cstrSpec.guard.ErrorGuard;
import btrplace.solver.api.cstrSpec.guard.Guard;
import btrplace.solver.api.cstrSpec.guard.MaxTestsGuard;
import btrplace.solver.api.cstrSpec.guard.TimeGuard;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;

import java.util.ArrayList;
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

    @Override
    public CTestCaseResult next() {

        CTestCase tc = in.next();

        CheckerResult specRes;
        CheckerResult res;
        if (continuous) {
            specRes = specVerifier.verify(tc.getConstraint(), tc.getPlan(), tc.getParameters());
            res = verifier.verify(tc.getConstraint(), tc.getPlan(), tc.getParameters());
        } else {
            Model src = tc.getPlan().getOrigin();
            Model dst = tc.getPlan().getResult();
            specRes = specVerifier.verify(tc.getConstraint(), src, dst, tc.getParameters());
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
        return prev;
    }

    @Override
    public Iterator<CTestCaseResult> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
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

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
