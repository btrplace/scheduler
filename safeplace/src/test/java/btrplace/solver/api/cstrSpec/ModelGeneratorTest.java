package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.generator.ModelGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ModelGeneratorTest {

    @Test
    public void testAll() {
        ModelGenerator gen = new ModelGenerator();
        List<Model> all = gen.all(2, 2);
        //Assert.assertEquals(all.size(), (int)Math.pow(2, 3));
        for (Model m : all) {
            System.out.println(m.getMapping());
        }
        Assert.fail();
    }
}
