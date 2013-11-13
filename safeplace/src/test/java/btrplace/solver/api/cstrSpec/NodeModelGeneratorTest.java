package btrplace.solver.api.cstrSpec;

import btrplace.model.DefaultElementBuilder;
import btrplace.model.Model;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class NodeModelGeneratorTest {

    @Test
    public void test() {
        NodeModelGenerator ng = new NodeModelGenerator(new DefaultElementBuilder(), 4);
        Set<Model> s = new HashSet<>();
        for (Model m : ng) {
            Assert.assertTrue(s.add(m));
        }
        Assert.assertEquals(s.size(), (int)Math.pow(2, 4));
    }
}
