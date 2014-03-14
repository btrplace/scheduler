package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.fuzzer.ModelsGenerator;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * @author Fabien Hermenier
 */
public class ParallelConstraintVerificationNGTest {

    public Specification getSpec() throws Exception {
        SpecReader r = new SpecReader();
        return r.getSpecification(new File("src/main/cspec/v1.cspec"));
    }

    @Test
    public void test() throws Exception {
        Specification s = getSpec();
        ModelsGenerator mg = new ModelsGenerator(2, 2);

        Constraint c = s.get("lonely");
        System.out.println(c.pretty());
        ParallelConstraintVerificationNG pc = new ParallelConstraintVerificationNG(mg, new ImplVerifier(), 3, c, false);
        long st = System.currentTimeMillis();
        int nb = pc.verify();
        long ed = System.currentTimeMillis();
        System.err.println("Computed in " + (ed - st) + " ms");
        System.out.println(pc.getDefiant().size() + "/" + nb);
        for (TestCase tc : pc.getDefiant()) {
            System.out.println(tc.pretty(true));
        }
        Assert.fail();
    }
}
