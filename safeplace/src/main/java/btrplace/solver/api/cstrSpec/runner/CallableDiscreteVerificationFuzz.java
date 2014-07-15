package btrplace.solver.api.cstrSpec.runner;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.fuzzer.ConstraintInputFuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer;
import btrplace.solver.api.cstrSpec.guard.Guard;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
class CallableDiscreteVerificationFuzz implements CallableVerification {

    private Constraint c;

    private Verifier ve;

    private List<VerifDomain> vDoms;

    private ReconfigurationPlanFuzzer fuzz;

    private List<Guard> guards;

    private boolean stop;

    private ParallelConstraintVerificationFuzz master;

    public CallableDiscreteVerificationFuzz(ParallelConstraintVerificationFuzz master, ReconfigurationPlanFuzzer fuzz, Verifier ve, List<VerifDomain> vDoms, Constraint c) {
        this.master = master;
        this.fuzz = fuzz;
        stop = false;
        this.c = c;
        this.ve = ve;
        this.vDoms = vDoms;
        guards = new ArrayList<>();
    }

    @Override
    public Boolean call() {
        SpecVerifier specVerifier = new SpecVerifier();

        //A fake spec to generate the args
        SpecModel s = new SpecModel(fuzz.newModel());
        ConstraintInputFuzzer cig = new ConstraintInputFuzzer(c, s);

        while (!stop) {
            Model src = fuzz.newModel();
            Model dst = fuzz.newModel();

            List<Constant> args = cig.newParams();
            CheckerResult specRes = specVerifier.verify(c, src, dst, args);
            CheckerResult againstRes = ve.verify(c, src, dst, args);
            TestCase tc = new TestCase(specRes, againstRes, ve, this.c, src, dst, args, true);
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
                e.printStackTrace();
                if (!master.commitDefiant(tc)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void stop() {
        stop = true;
    }
}
