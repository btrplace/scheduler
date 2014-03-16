package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.fuzzer.DelaysGenerator;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlansGenerator;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Fabien Hermenier
 */
class CallableVerification implements Callable<List<TestCase>[]> {

    private Model mo;

    private boolean continuous;

    private Constraint c;

    private Verifier ve;

    public CallableVerification(Verifier ve, Model mo, Constraint c, boolean cont) {
        this.mo = mo;
        this.c = c;
        this.ve = ve;
        this.continuous = cont;
    }

    @Override
    public List<TestCase>[] call() {
        System.err.println("start");
        ReconfigurationPlansGenerator grn = new ReconfigurationPlansGenerator(mo, 2);
        SpecModel sp = new SpecModel(mo);
        List<List<Constant>> allArgs = new ArrayList<>();
        ConstraintInputGenerator cig = new ConstraintInputGenerator(this.c, sp, true);
        for (List<Constant> args : cig) {
            allArgs.add(args);
        }
        List<TestCase>[] res = new List[2];
        res[0] = new ArrayList<>();
        res[1] = new ArrayList<>();
        long st = System.currentTimeMillis();
        for (ReconfigurationPlan skelPlan : grn) {
            //Every input
            List<ReconfigurationPlan> delayed = new ArrayList<>();
            if (continuous) {
                for (ReconfigurationPlan drp : new DelaysGenerator(skelPlan)) {
                    delayed.add(drp);
                }
            } else {
                delayed.add(skelPlan);
            }
            for (ReconfigurationPlan p : delayed) {
                for (List<Constant> args : allArgs) {
                    TestCase tc = new TestCase(ve, this.c, p, args, !continuous);
                    try {
                        if (tc.succeed()) {
                            res[0].add(tc);
                        } else {
                            res[1].add(tc);
                        }
                    } catch (Exception e) {
                        res[1].add(tc);
                    }
                }
            }
        }
        long ed = System.currentTimeMillis();
        System.err.println("Stop: " + (ed - st));
        return res;
    }
}
