package btrplace.solver.api.cstrSpec.runner;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer2;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
class CallableDiscreteVerificationFuzz2 extends DefaultCallableVerification {

    public CallableDiscreteVerificationFuzz2(ParallelConstraintVerificationFuzz master, ReconfigurationPlanFuzzer2 fuzz, Verifier ve, List<VerifDomain> vDoms, Constraint c) {
        super(master, fuzz, ve, vDoms, c);
    }

    public TestCase runTest(ReconfigurationPlan p, List<Constant> args) {
        //CheckerResult specRes = specVerifier.verify(c, args, p);
        //CheckerResult againstRes = ve.verify(c, args, p);
        return new TestCase(null, null, ve, this.c, p, args, true);
    }

}
