package btrplace.solver.api.cstrSpec;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.generator.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class PlanGeneratorTest {

    public static Model makeModel() {
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
        return m;
    }
    @Test
    public void testActionsGenerator(){
        PlanGenerator pg = new PlanGenerator();
        Model mo = makeModel();
        List<ReconfigurationPlan> plans = pg.anyAction(mo);
        Assert.assertEquals(plans.size(), (int)Math.pow(2, mo.getMapping().getNbNodes()) * 40);
    }

    @Test
    public void testAnyDurations() {
        Model mo = makeModel();
        PlanGenerator pg = new PlanGenerator();
        for (ReconfigurationPlan p : pg.anyAction(mo)) {
            Assert.assertEquals(pg.anyDuration(p, 1, 3).size(), (int)Math.pow(3, p.getSize()));
        }
    }

   @Test
   public void gogogo() {
       ModelGenerator mg = new ModelGenerator();
       PlanGenerator pg = new PlanGenerator();
       List<ReconfigurationPlan> all = new ArrayList<>();
       for (Model mo : mg.all(2, 2)) {
           for (ReconfigurationPlan p : pg.anyAction(mo)) {
               for (ReconfigurationPlan p2 : pg.anyDuration(p, 1, 3)) {
                   all.addAll(pg.anyDelay(p2));
                   System.err.println(all.size());
               }
           }
       }
   }

    @Test
    public void gogogo2() {
        int nb = 0;
        ModelsGenerator mg = new ModelsGenerator(2, 2);
        for (Model mo : mg) {
            ReconfigurationPlansGenerator rpg = new ReconfigurationPlansGenerator(mo);
            for (ReconfigurationPlan rp : rpg) {
                DurationsGenerator tg = new DurationsGenerator(rp, 1, 3);
                for (ReconfigurationPlan rp2 : tg) {
                    DelaysGenerator dg = new DelaysGenerator(rp2);
                    for (ReconfigurationPlan rp3 : dg) {
                        nb++;
                        if (nb%1000 == 0) {
                            System.out.println(nb);
                        }
                    }
                }
            }
        }
    }

/*
        for (ReconfigurationPlan p : plans) {
            System.err.println(m.getMapping());
            Assert.assertEquals(pg.planWithVMs(p).size(), 40);
            for (ReconfigurationPlan p4 : pg.planWithVMs(p)) {
                System.err.println(p4);
            }
            //Assert.fail();
            System.err.println(p);
            List<ReconfigurationPlan> variations = pg.anyDuration(p, 1, 3);
            Assert.assertEquals(variations.size(), (int)Math.pow(3, p.getSize()));
            for (ReconfigurationPlan p2 : pg.anyDuration(p, 1, 3)) {
                System.err.println("Derivative:" + p2);
                List<ReconfigurationPlan> delayed = pg.anyDelay(p2);
                for (ReconfigurationPlan p3 : pg.anyDelay(p2)) {
                    System.err.println("Delayed derivative: " + p3);
                }
            }
        }
        Assert.fail();*/
}
