package btrplace.solver.api.cstrSpec.generator;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class VMModelsGeneratorTest {

    @Test
    public void simpleTest() {
        Model mo = new DefaultModel();
        for (int i = 0; i < 3; i++) {
            mo.getMapping().addOnlineNode(mo.newNode());
        }
        VMModelsGenerator mg = new VMModelsGenerator(mo, 3);
        Set<Model> s = new HashSet<>();
        for (Model m : mg) {
            Assert.assertTrue(s.add(m));
        }
        Assert.assertEquals(s.size(), 343);
    }
}
