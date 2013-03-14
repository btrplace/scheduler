package btrplace.plan;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.MigrateVM;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Unit tests for {@link DefaultReconfigurationPlanExecutor}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlanExecutorTest implements PremadeElements {

    @Test
    public void testSimple() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);
        map.addReadyVM(vm3);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);

        Model mo = new DefaultModel(map);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);

        BootNode a1 = new BootNode(n3, 0, 3); //no deps
        BootVM a2 = new BootVM(vm3, n1, 0, 3); //no deps
        MigrateVM a3 = new MigrateVM(vm1, n1, n3, 4, 5); //deps: a1
        MigrateVM a4 = new MigrateVM(vm2, n2, n1, 4, 7); //no deps

        plan.add(a1);
        plan.add(a3);
        plan.add(a2);
        plan.add(a4);

        //Just in case
        Assert.assertTrue(plan.isApplyable(), '\n' + plan.toString());
        ReconfigurationPlanExecutor exec = new DefaultReconfigurationPlanExecutor(plan);
        Assert.assertEquals(exec.getCurrentModel(), mo);
        Assert.assertEquals(exec.getFeasibleActions().size(), 3, exec.getFeasibleActions().toString());
        Assert.assertTrue(exec.getFeasibleActions().containsAll(Arrays.asList(a1, a2, a4)), exec.getFeasibleActions().toString());
        Assert.assertFalse(exec.isOver());

        Assert.assertEquals(exec.getWaitingActions().size(), 1, exec.getFeasibleActions().toString());
        Assert.assertTrue(exec.getWaitingActions().containsAll(Arrays.asList(a3)), exec.getFeasibleActions().toString());


        Assert.assertTrue(exec.commit(a4));
        Assert.assertFalse(exec.isOver());
        Assert.assertFalse(exec.commit(a3));
        Assert.assertFalse(exec.isOver());
        Assert.assertEquals(exec.getFeasibleActions().size(), 2);
        Assert.assertTrue(exec.getFeasibleActions().containsAll(Arrays.asList(a1, a2)), exec.getFeasibleActions().toString());

        Assert.assertTrue(exec.commit(a1));
        Assert.assertFalse(exec.isOver());
        Assert.assertEquals(exec.getFeasibleActions().size(), 2);
        Assert.assertTrue(exec.getFeasibleActions().containsAll(Arrays.asList(a2, a3)), exec.getFeasibleActions().toString());
        Assert.assertTrue(exec.getWaitingActions().isEmpty());

        Assert.assertTrue(exec.commit(a2));
        Assert.assertFalse(exec.isOver());
        Assert.assertTrue(exec.commit(a3));
        Assert.assertTrue(exec.isOver());


    }
}
