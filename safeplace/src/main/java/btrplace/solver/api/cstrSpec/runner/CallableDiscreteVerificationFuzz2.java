package btrplace.solver.api.cstrSpec.runner;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.CheckerResult;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
class CallableDiscreteVerificationFuzz2 extends DefaultCallableVerification {

    public CallableDiscreteVerificationFuzz2(ParallelConstraintVerificationFuzz master, ReconfigurationPlanFuzzer fuzz, Verifier ve, List<VerifDomain> vDoms, Constraint c) {
        super(master, fuzz, ve, vDoms, c);
    }

    public TestCase runTest(ReconfigurationPlan p, List<Constant> args) {
        Model src = p.getOrigin();
        Model dst = p.getResult();

        CheckerResult specRes = specVerifier.verify(c, src, dst, args);
        CheckerResult againstRes = ve.verify(c, src, dst, args);
        return new TestCase(specRes, againstRes, ve, this.c, p, args, true);
    }

}
