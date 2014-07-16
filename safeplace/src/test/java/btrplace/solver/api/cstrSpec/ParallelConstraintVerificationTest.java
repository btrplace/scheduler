package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.backend.InMemoryBackend;
import btrplace.solver.api.cstrSpec.fuzzer.ModelsGenerator;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer;
import btrplace.solver.api.cstrSpec.fuzzer.TransitionTable;
import btrplace.solver.api.cstrSpec.guard.MaxTestsGuard;
import btrplace.solver.api.cstrSpec.runner.ParallelConstraintVerification;
import btrplace.solver.api.cstrSpec.runner.ParallelConstraintVerificationFuzz;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;
import edu.emory.mathcs.backport.java.util.Collections;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ParallelConstraintVerificationTest {

    public Specification getSpec() throws Exception {
        SpecReader r = new SpecReader();
        return r.getSpecification(new File("src/main/cspec/v1.cspec"));
    }

    @Test
    public void test() throws Exception {
        Specification s = getSpec();
        ModelsGenerator mg = new ModelsGenerator(4, 4);
        Constraint c = s.get("split");
        System.out.println(c.pretty());
        ParallelConstraintVerification pc = new ParallelConstraintVerification(mg, Collections.<VerifDomain>emptyList(), new ImplVerifier(), 20, c, true, true);
        long st = System.currentTimeMillis();
        pc.verify();
        long ed = System.currentTimeMillis();
        System.err.println("Computed in " + (ed - st) + " ms");
        /*int nb = pc.getDefiant().size() + pc.getCompliant().size();
        System.out.println(pc.getDefiant().size() + "/" + nb);
        for (TestCase tc : pc.getDefiant()) {
            System.out.println(tc.pretty(true));
        } */
        Assert.fail();
    }

    @Test
    public void testFuzz() throws Exception {
        String root = "src/main/bin/";
        Specification s = getSpec();
        ReconfigurationPlanFuzzer fuzz = new ReconfigurationPlanFuzzer(new TransitionTable(new FileReader(root + "node_transitions")),
                new TransitionTable(new FileReader(root + "vm_transitions")), 2, 1);
        Constraint c = s.get("root");
        System.out.println(c.pretty());
        List<VerifDomain> doms = new ArrayList<>();
        //doms.add(new IntVerifDomain(0, 10));
        ParallelConstraintVerificationFuzz pc = new ParallelConstraintVerificationFuzz(fuzz, doms, new ImplVerifier(), c);
        InMemoryBackend b = new InMemoryBackend();
        pc.setBackend(b);
        pc.limit(new MaxTestsGuard(10000));
        //pc.limit(new TimeGuard(60));
        pc.setNbWorkers(3);
        pc.setContinuous(true);
        for (Constraint x : s.getConstraints()) {
            if (x.isCore() && x != c) {
                pc.precondition(x);
            }
        }
        pc.verify();
        int nb = b.getDefiant().size() + b.getCompliant().size();
        System.out.println(b.getDefiant().size() + "/" + nb);
        for (TestCase tc : b.getDefiant()) {
            System.out.println(tc.pretty(true));
        }
        Assert.fail();
    }
}
