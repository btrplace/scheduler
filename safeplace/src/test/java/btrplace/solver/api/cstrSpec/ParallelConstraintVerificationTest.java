package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlanFuzzer;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
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
        ReconfigurationPlanFuzzer fuzzer = new ReconfigurationPlanFuzzer(2, 2).minDuration(1).maxDuration(3).nbDurations(3).nbDelays(3).discrete();

        Constraint c = s.get("among");
        System.out.println(c.pretty());
        ParallelConstraintVerification pc = new ParallelConstraintVerification(fuzzer, new ImplVerifier(), 4, c, false);
        long st = System.currentTimeMillis();
        List<TestCase> issues = pc.verify();
        long ed = System.currentTimeMillis();
        System.err.println("Computed in " + (ed - st) + " ms");
        Assert.assertEquals(issues.size(), 19);
        Assert.fail();
    }
}
