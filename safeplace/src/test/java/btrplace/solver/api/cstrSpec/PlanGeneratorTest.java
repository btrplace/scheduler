package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.generator.DelaysGenerator;
import btrplace.solver.api.cstrSpec.generator.DurationsGenerator;
import btrplace.solver.api.cstrSpec.generator.ModelsGenerator;
import btrplace.solver.api.cstrSpec.generator.ReconfigurationPlansGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class PlanGeneratorTest {

    @Test
    public void gogogo() {
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
                /*        if (nb%1000 == 0) {
                            System.out.println(nb);
                        }*/
                    }
                }
            }
        }
        Assert.fail(""+nb);
    }
}
