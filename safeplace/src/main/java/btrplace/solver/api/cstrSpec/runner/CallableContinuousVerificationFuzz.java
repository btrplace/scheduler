package btrplace.solver.api.cstrSpec.runner;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.fuzzer.ConstraintInputFuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.DelaysGeneratorFuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;

import java.util.Collections;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
class CallableContinuousVerificationFuzz implements CallableVerification {

    private Constraint c;

    private Verifier ve;

    private List<VerifDomain> vDoms;

    private ReconfigurationPlanFuzzer fuzz;

    private boolean stop;

    private ParallelConstraintVerificationFuzz master;

    public CallableContinuousVerificationFuzz(ParallelConstraintVerificationFuzz master, ReconfigurationPlanFuzzer fuzz, Verifier ve, List<VerifDomain> vDoms, Constraint c) {
        this.master = master;
        this.c = c;
        this.ve = ve;
        this.vDoms = vDoms;
        this.fuzz = fuzz;
        stop = false;
    }

    @Override
    public Boolean call() {
        SpecVerifier specVerifier = new SpecVerifier();

        //A fake spec to generate the args
        SpecModel s = new SpecModel(fuzz.newModel());
        ConstraintInputFuzzer cig = new ConstraintInputFuzzer(c, s);

        while (!stop) {
            ReconfigurationPlan skelPlan = fuzz.newPlan();
            ReconfigurationPlan p = DelaysGeneratorFuzzer.newDelayed(skelPlan);
            if (!checkPre(p)) {
                continue;
            }
            List<Constant> args = cig.newParams();
            CheckerResult specRes = specVerifier.verify(c, p, args);
            CheckerResult againstRes = ve.verify(c, p, args);

            TestCase tc = new TestCase(specRes, againstRes, ve, this.c, p, args, false);
            try {
                if (tc.succeed()) {
                    if (!master.commitCompliant(tc)) {
                        return false;
                    }
                } else {
                    if (!master.commitDefiant(tc)) {
                        return false;
                    }
                }
            } catch (Exception e) {
                if (!master.commitDefiant(tc)) {
                    return false;
                }

            }
        }
        return true;
    }

    private boolean checkPre(ReconfigurationPlan p) {
        SpecVerifier spec = new SpecVerifier();
        for (Constraint c : master.preconditions()) {
            CheckerResult res = spec.verify(c, p, Collections.<Constant>emptyList());
            if (!res.getStatus()) {
                return false;
            }
        }
        for (Constraint c : master.preconditions()) {
            CheckerResult res = ve.verify(c, p, Collections.<Constant>emptyList());
            if (!res.getStatus()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void stop() {
        stop = true;
    }
}
