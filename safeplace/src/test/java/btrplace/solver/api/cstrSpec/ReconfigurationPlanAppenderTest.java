package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.fuzzer.ModelsGenerator;
import btrplace.solver.api.cstrSpec.fuzzer.ReconfigurationPlansGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link btrplace.solver.api.cstrSpec.ReconfigurationPlanAppender}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanAppenderTest {

    @Test
    public void test() {
        ModelsGenerator mg = new ModelsGenerator(2, 1);
        ReconfigurationPlanAppender app = new ReconfigurationPlanAppender();
        int nbActions = 0;
        for (Model mo : mg) {
            ReconfigurationPlansGenerator pg = new ReconfigurationPlansGenerator(mo, 3);
            //System.out.println("Current:\n" + app.getResult());
            for (ReconfigurationPlan p : pg) {
                nbActions += p.getSize();
                //  System.out.println("+ plan:\n" + p);
                app.append(p);
                //System.out.println("=\n" + app.getResult());
            }
        }
        System.out.println(app.getResult());
        Assert.assertEquals(app.getResult().getSize(), nbActions);

    }
}
