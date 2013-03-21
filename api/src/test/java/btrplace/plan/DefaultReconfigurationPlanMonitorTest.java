package btrplace.plan;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.view.ShareableResource;
import btrplace.plan.event.*;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;

/**
 * Unit tests for {@link DefaultReconfigurationPlanMonitor}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlanMonitorTest implements PremadeElements {

    static BootNode a1 = new BootNode(n3, 0, 3); //no deps
    static BootVM a2 = new BootVM(vm3, n1, 0, 3); //no deps
    static MigrateVM a3 = new MigrateVM(vm1, n1, n3, 4, 5); //deps: a1
    static MigrateVM a4 = new MigrateVM(vm2, n2, n1, 4, 7); //no deps


    private static ReconfigurationPlan makePlan() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);
        map.addReadyVM(vm3);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);

        Model mo = new DefaultModel(map);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);

        plan.add(a1);
        plan.add(a3);
        plan.add(a2);
        plan.add(a4);

        Assert.assertTrue(plan.isApplyable(), '\n' + plan.toString());
        return plan;
    }

    @Test
    public void testInit() {

        ReconfigurationPlan plan = makePlan();
        ReconfigurationPlanMonitor exec = new DefaultReconfigurationPlanMonitor(plan);

        Assert.assertEquals(exec.getCurrentModel(), plan.getOrigin());
        Assert.assertFalse(exec.isBlocked(a1));
        Assert.assertFalse(exec.isBlocked(a2));
        Assert.assertTrue(exec.isBlocked(a3));
        Assert.assertFalse(exec.isBlocked(a4));
        Assert.assertFalse(exec.isOver());
    }

    @Test(dependsOnMethods = {"testInit"})
    public void testBegin() {
        ReconfigurationPlan plan = makePlan();
        ReconfigurationPlanMonitor exec = new DefaultReconfigurationPlanMonitor(plan);

        //Begin a feasible action. Should work
        Assert.assertTrue(exec.begin(a4));

        //Begin a blocked action. Fail
        Assert.assertFalse(exec.begin(a3));

        //Begin a pending action. Fail
        Assert.assertFalse(exec.begin(a4));
    }

    @Test(dependsOnMethods = {"testInit", "testBegin"})
    public void testGoodCommits() throws ReconfigurationPlanMonitorException {
        ReconfigurationPlan plan = makePlan();
        ReconfigurationPlanMonitor exec = new DefaultReconfigurationPlanMonitor(plan);

        Assert.assertTrue(exec.begin(a4));
        Assert.assertTrue(exec.begin(a1));

        Assert.assertTrue(exec.commit(a4).isEmpty());
        Set<Action> released = exec.commit(a1);
        Assert.assertEquals(released.size(), 1);
        Assert.assertTrue(released.contains(a3));
        Assert.assertFalse(exec.isBlocked(a3));
    }

    @Test(dependsOnMethods = {"testInit", "testBegin", "testGoodCommits"}, expectedExceptions = {ReconfigurationPlanMonitorException.class})
    public void testCommitUnStarted() throws ReconfigurationPlanMonitorException {
        ReconfigurationPlan plan = makePlan();
        ReconfigurationPlanMonitor exec = new DefaultReconfigurationPlanMonitor(plan);
        exec.commit(a1);
    }

    @Test(dependsOnMethods = {"testInit", "testBegin", "testGoodCommits"}, expectedExceptions = {ReconfigurationPlanMonitorException.class})
    public void testDoubleCommit() throws ReconfigurationPlanMonitorException {
        ReconfigurationPlan plan = makePlan();
        ReconfigurationPlanMonitor exec = new DefaultReconfigurationPlanMonitor(plan);
        exec.begin(a1);
        try {
            exec.commit(a1);
        } catch (ReconfigurationPlanMonitorException ex) {
            Assert.fail();
        }
        exec.commit(a1);
    }

    @Test(dependsOnMethods = {"testInit", "testBegin", "testGoodCommits"})
    public void testOver() throws ReconfigurationPlanMonitorException {
        ReconfigurationPlan plan = makePlan();
        ReconfigurationPlanMonitor exec = new DefaultReconfigurationPlanMonitor(plan);

        Assert.assertTrue(exec.begin(a1));
        Assert.assertTrue(exec.begin(a4));
        Assert.assertTrue(exec.begin(a2));
        Assert.assertTrue(exec.commit(a2).isEmpty());
        Assert.assertTrue(exec.commit(a4).isEmpty());
        Assert.assertFalse(exec.commit(a1).isEmpty());
        Assert.assertTrue(exec.begin(a3));
        Assert.assertFalse(exec.isOver());
        Assert.assertTrue(exec.commit(a3).isEmpty());
        Assert.assertTrue(exec.isOver());
    }

    @Test
    public void testComplex() {
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

        Assert.assertTrue(plan.isApplyable());
    }
}
