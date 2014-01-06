package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.plan.ReconfigurationPlan;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class ReconfigurationPlansGeneratorTest {

    @Test
    public void test() {
        Model m = new DefaultModel();
        Node n1 = m.newNode();
        Node n2 = m.newNode();
        Node n3 = m.newNode();
        m.getMapping().addOnlineNode(n1);
        m.getMapping().addOnlineNode(n2);
        m.getMapping().addOfflineNode(n3);

        m.getMapping().addReadyVM(m.newVM());
        m.getMapping().addRunningVM(m.newVM(), n1);
        m.getMapping().addSleepingVM(m.newVM(), n2);

        ReconfigurationPlansGenerator pg = new ReconfigurationPlansGenerator(m);
        Set<ReconfigurationPlan> s = new HashSet<>();
        for (ReconfigurationPlan rp : pg) {
            Assert.assertTrue(s.add(rp));
        }
        Assert.assertEquals(s.size(), (int) Math.pow(2, m.getMapping().getNbNodes()) * 40);
    }
}
