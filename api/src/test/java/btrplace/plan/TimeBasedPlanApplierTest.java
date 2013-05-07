package btrplace.plan;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.view.ShareableResource;
import btrplace.plan.event.Allocate;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownNode;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests {@link TimeBasedPlanApplier}.
 *
 * @author Fabien Hermenier
 */
public class TimeBasedPlanApplierTest implements PremadeElements {

    private static ReconfigurationPlan makePlan() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOfflineNode(n4);

        map.addRunningVM(vm1, n3);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n2);
        BootNode bN4 = new BootNode(n4, 3, 5);
        MigrateVM mVM1 = new MigrateVM(vm1, n3, n4, 6, 7);
        Allocate aVM3 = new Allocate(vm3, n2, "cpu", 7, 8, 9);
        MigrateVM mVM2 = new MigrateVM(vm2, n1, n2, 1, 3);
        MigrateVM mVM4 = new MigrateVM(vm4, n2, n3, 1, 7);
        ShutdownNode sN1 = new ShutdownNode(n1, 5, 7);

        ShareableResource rc = new ShareableResource("cpu");
        rc.set(vm3, 3);

        Model mo = new DefaultModel(map);
        mo.attach(rc);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        plan.add(bN4);
        plan.add(mVM1);
        plan.add(aVM3);
        plan.add(mVM2);
        plan.add(mVM4);
        plan.add(sN1);
        return plan;
    }

    @Test
    public void testApply() {
        ReconfigurationPlan plan = makePlan();
        Model res = new DependencyBasedPlanApplier().apply(plan);
        Mapping resMapping = res.getMapping();
        Assert.assertTrue(resMapping.getOfflineNodes().contains(n1));
        Assert.assertTrue(resMapping.getOnlineNodes().contains(n4));
        ShareableResource rc = (ShareableResource) res.getView(ShareableResource.VIEW_ID_BASE + "cpu");
        Assert.assertEquals(rc.get(vm3), 7);
        Assert.assertEquals(resMapping.getVMLocation(vm1), n4);
        Assert.assertEquals(resMapping.getVMLocation(vm2), n2);
        Assert.assertEquals(resMapping.getVMLocation(vm4), n3);
    }

    @Test
    public void testToString() {
        ReconfigurationPlan plan = makePlan();
        Assert.assertFalse(new DependencyBasedPlanApplier().toString(plan).contains("null"));
    }
}
