package btrplace.solver.api.cstrSpec;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.fuzzer.ConstraintInputGenerator;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * @author Fabien Hermenier
 */
public class ConstraintInputGeneratorTest {

    private static Constraint getConstraint(String file, String id) {
        Specification spec = null;
        try {
            SpecReader r = new SpecReader();
            spec = r.getSpecification(new File(file));
            return spec.get(id);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        return null;
    }

    @Test
    public void test() throws Exception {
        Constraint sp = getConstraint("src/main/cspec/v1.cspec", "among");
        Model mo = new DefaultModel();
        mo.getMapping().addOnlineNode(mo.newNode());
        mo.getMapping().addOnlineNode(mo.newNode());
        mo.getMapping().addReadyVM(mo.newVM());
        mo.getMapping().addReadyVM(mo.newVM());
        ConstraintInputGenerator cig = new ConstraintInputGenerator(sp, new SpecModel(mo), true);
        /*while (cig.hasNext()) {
            System.out.println(cig.next());
        } */
        Assert.assertEquals(cig.count(), 12);
    }
}
