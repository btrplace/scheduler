package btrplace.solver.api.cstrSpec.generator;

import btrplace.model.DefaultElementBuilder;
import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.generator.NodeModelsGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class NodeModelsGeneratorTest {

    @Test
    public void test() {
        NodeModelsGenerator ng = new NodeModelsGenerator(new DefaultElementBuilder(), 4);
        Set<Model> s = new HashSet<>();
        for (Model m : ng) {
            Assert.assertTrue(s.add(m));
        }
        Assert.assertEquals(s.size(), (int)Math.pow(2, 4));
    }
}
