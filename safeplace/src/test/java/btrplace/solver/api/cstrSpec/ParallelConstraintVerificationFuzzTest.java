package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.backend.ReducedDefiantStore;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.TransitionTable;
import btrplace.solver.api.cstrSpec.guard.MaxTestsGuard;
import btrplace.solver.api.cstrSpec.reducer.ElementsReducer;
import btrplace.solver.api.cstrSpec.reducer.PlanReducer;
import btrplace.solver.api.cstrSpec.runner.ParallelConstraintVerificationFuzz;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.IntVerifDomain;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ParallelConstraintVerificationFuzzTest {

    public Specification getSpec() throws Exception {
        SpecReader r = new SpecReader();
        return r.getSpecification(new File("src/main/cspec/v1.cspec"));
    }

    @Test
    public void testFuzz() throws Exception {
        String root = "src/main/bin/";
        Specification s = getSpec();
        ReconfigurationPlanFuzzer fuzz = new ReconfigurationPlanFuzzer(new TransitionTable(new FileReader(root + "node_transitions")),
                new TransitionTable(new FileReader(root + "vm_transitions")), 3, 3);
        Constraint c = s.get("maxOnline");
        System.out.println(c.pretty());
        List<VerifDomain> doms = new ArrayList<>();
        doms.add(new IntVerifDomain(0, 5));
        ParallelConstraintVerificationFuzz pc = new ParallelConstraintVerificationFuzz(fuzz, doms, new ImplVerifier(true), c);
        ReducedDefiantStore b = new ReducedDefiantStore();
        b.reduceWith(new PlanReducer());
        b.reduceWith(new ElementsReducer());
        pc.setBackend(b);
        pc.limit(new MaxTestsGuard(10000));
        //pc.limit(new TimeGuard(60));
        pc.setNbWorkers(1);
        pc.setContinuous(true);
        for (Constraint x : s.getConstraints()) {
            if (x.isCore() && x != c) {
                pc.precondition(x);
            }
        }
        pc.verify();
        int nb = b.getDefiant().size() + b.getCompliant().size();
        System.out.println(b.getDefiant().size() + "/" + nb);

        int falseOk = 0, falseKo = 0;

        for (TestCase tc : b.getDefiant()) {
            //    System.out.println(tc.pretty(true));
            if (tc.falsePositive()) {
                falseOk++;
            } else if (tc.falseNegative()) {
                falseKo++;
            } else {
                System.err.println("Buggy: " + tc.pretty(false));
            }
        }
        System.out.println(falseOk + "false positives; " + falseKo + " false negatives");
        Assert.fail();
    }
}
