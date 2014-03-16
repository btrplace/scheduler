package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.fuzzer.ModelsGenerator;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import edu.emory.mathcs.backport.java.util.Collections;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

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
        ModelsGenerator mg = new ModelsGenerator(2, 2);

        Constraint c = s.get("noVMsOnOfflineNodes");
        System.out.println(c.pretty());
        ParallelConstraintVerification pc = new ParallelConstraintVerification(mg, Collections.emptyList(), new ImplVerifier(), 3, c, true, false);
        long st = System.currentTimeMillis();
        pc.verify();
        long ed = System.currentTimeMillis();
        System.err.println("Computed in " + (ed - st) + " ms");
        int nb = pc.getDefiant().size() + pc.getCompliant().size();
        System.out.println(pc.getDefiant().size() + "/" + nb);
        /*for (TestCase tc : pc.getDefiant()) {
            System.out.println(tc.pretty(true));
        } */
        Assert.fail();
    }
}
