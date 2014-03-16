package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.Specification;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class ModelsGeneratorTest {

    @Test
    public void test() {
        ModelsGenerator mg = new ModelsGenerator(3, 3);
        Set<Model> s = new HashSet<>();
        for (Model mo : mg) {
            Assert.assertTrue(s.add(mo));
        }

        Assert.assertEquals(s.size(), 800);
    }

    public Specification getSpec() throws Exception {
        SpecReader r = new SpecReader();
        return r.getSpecification(new File("src/main/cspec/v1.cspec"));
    }

    @Test
    public void testFromSpec() throws Exception {
        Specification s = getSpec();
        for (Constraint c : s.getConstraints()) {
            ModelsGenerator mg = ModelsGenerator.makeFromSpec(c);
            System.out.println(c.pretty());
            System.out.println(mg.getNbVMs() + "x" + mg.getNbNodes());
        }
    }
}
