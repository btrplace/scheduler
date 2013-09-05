package btrplace.model.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.ShutdownNode;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MaxOnlineTest {

    @Test
    public void isSatisfiedModel() {
        Model model = new DefaultModel();
        Mapping map = model.getMapping();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        Set<Node> s = new HashSet<Node>(Arrays.asList(n1, n2, n3));
        MaxOnline mo = new MaxOnline(s, 2);

        Assert.assertTrue(mo.isSatisfied(model));

        model.getMapping().addOnlineNode(n3);
        Assert.assertFalse(mo.isSatisfied(model));
    }

    @Test
    public void isSatisfiedReconfigurationPlan() {
        Model model = new DefaultModel();
        Mapping map = model.getMapping();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        Set<Node> s = new HashSet<Node>(Arrays.asList(n1, n2, n3));
        MaxOnline mo = new MaxOnline(s, 2);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(model);

        Assert.assertTrue(mo.isSatisfied(plan));

        plan.add(new BootNode(n3, 3, 9));
        Assert.assertFalse(mo.isSatisfied(plan));

        plan.add(new ShutdownNode(n2, 0, 5));
        Assert.assertTrue(mo.isSatisfied(plan));

    }
}
