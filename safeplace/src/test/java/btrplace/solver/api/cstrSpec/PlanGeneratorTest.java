package btrplace.solver.api.cstrSpec;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.ReconfigurationPlan;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class PlanGeneratorTest {

    @Test
    public void test(){
        Model m  = new DefaultModel();
        Node n1 = m.newNode();
        Node n2 = m.newNode();
        Node n3 = m.newNode();
        VM vm1 = m.newVM();
        VM vm2 = m.newVM();
        VM vm3 = m.newVM();
        m.getMapping().addOnlineNode(n1);
        m.getMapping().addOnlineNode(n2);
        m.getMapping().addOfflineNode(n3);
        m.getMapping().addReadyVM(vm1);
        m.getMapping().addRunningVM(vm2, n1);
        m.getMapping().addSleepingVM(vm3, n2);
        PlanGenerator pg = new PlanGenerator();
        List<ReconfigurationPlan> plans = pg.plansWithNodeActions(m);
        Assert.assertEquals(plans.size(), (int)Math.pow(2, m.getMapping().getNbNodes()));
        for (ReconfigurationPlan p : plans) {
            System.err.println(m.getMapping());
            Assert.assertEquals(pg.planWithVMs(p).size(), 40);
            /*for (ReconfigurationPlan p4 : pg.planWithVMs(p)) {
                System.err.println(p4);
            } */
            //Assert.fail();
            System.err.println(p);
            List<ReconfigurationPlan> variations = pg.allDurations(p, 1, 3);
            Assert.assertEquals(variations.size(), (int)Math.pow(3, p.getSize()));
            for (ReconfigurationPlan p2 : pg.allDurations(p, 1, 3)) {
                System.err.println("Derivative:" + p2);
                List<ReconfigurationPlan> delayed = pg.anyDelay(p2);
                for (ReconfigurationPlan p3 : pg.anyDelay(p2)) {
                    System.err.println("Delayed derivative: " + p3);
                }
            }
        }
        Assert.fail();
    }
}
