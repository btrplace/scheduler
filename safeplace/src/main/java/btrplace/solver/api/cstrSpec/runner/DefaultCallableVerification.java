package btrplace.solver.api.cstrSpec.runner;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.fuzzer.ConstraintInputFuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.DelaysGeneratorFuzzer;
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
import java.util.Collections;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public abstract class DefaultCallableVerification implements CallableVerification {

    protected Constraint c;

    protected Verifier ve;

    private List<VerifDomain> vDoms;

    private ReconfigurationPlanFuzzer fuzz;

    private List<Guard> guards;

    private boolean stop;

    private ParallelConstraintVerificationFuzz master;

    protected SpecVerifier specVerifier = new SpecVerifier();

    public DefaultCallableVerification(ParallelConstraintVerificationFuzz master, ReconfigurationPlanFuzzer fuzz, Verifier ve, List<VerifDomain> vDoms, Constraint c) {
        this.master = master;
        this.fuzz = fuzz;
        stop = false;
        this.c = c;
        this.ve = ve;
        this.vDoms = vDoms;
        guards = new ArrayList<>();
    }

    @Override
    public void stop() {
        stop = true;
    }

    @Override
    public Boolean call() {

        //A fake spec to generate the args
        SpecModel s = new SpecModel(fuzz.newModel());
        for (VerifDomain vDom : vDoms) {
            s.add(vDom);
        }

        ConstraintInputFuzzer cig = new ConstraintInputFuzzer(c, s);

        while (!stop) {
            ReconfigurationPlan skel = fuzz.newPlan();
            ReconfigurationPlan p = DelaysGeneratorFuzzer.newDelayed(skel);
            if (!checkPre(p)) {
                continue;
            }
            TestCase tc = runTest(p, cig.newParams());
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

    public abstract TestCase runTest(ReconfigurationPlan p, List<Constant> args);

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
}
