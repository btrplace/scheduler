package btrplace.solver.api.cstrSpec.generator;

import btrplace.model.Model;
import org.testng.Assert;
import org.testng.annotations.Test;

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
}
