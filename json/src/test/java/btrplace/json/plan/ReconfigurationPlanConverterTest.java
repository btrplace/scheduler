package btrplace.json.plan;

import btrplace.json.JSONConverterException;
import btrplace.json.TestMaterial;
import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.MigrateVM;
import net.minidev.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ReconfigurationPlanConverter}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanConverterTest implements TestMaterial {

    @Test
    public void testConversion() throws JSONConverterException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOfflineNode(n2);
        map.addOnlineNode(n3);
        map.addReadyVM(vm1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n1);
        map.addSleepingVM(vm4, n3);
        map.addRunningVM(vm5, n3);

        Model mo = new DefaultModel(map);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        plan.add(new MigrateVM(vm2, n1, n3, 0, 1));
        plan.add(new BootVM(vm1, n3, 0, 1));
        plan.add(new BootNode(n2, 0, 5));

        ReconfigurationPlanConverter rcp = new ReconfigurationPlanConverter();
        JSONObject o = rcp.toJSON(plan);

        System.out.println(rcp.fromJSON(o));
        Assert.assertEquals(rcp.fromJSON(o), plan);

    }
}
