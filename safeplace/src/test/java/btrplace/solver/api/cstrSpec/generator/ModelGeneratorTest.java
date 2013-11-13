package btrplace.solver.api.cstrSpec.generator;

import btrplace.model.Model;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ModelGeneratorTest {

    @Test
    public void testAll() {
        ModelGenerator gen = new ModelGenerator();
        List<Model> all = gen.all(5, 5);
        System.err.println(all.size());
        /*int nb = 0;
        //Assert.assertEquals(all.size(), (int)Math.pow(2, 3));
        for (Model m : all) {
            nb++;
            System.out.println(m.getMapping());
        }
        Assert.fail();*/
    }
}
