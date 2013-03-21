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

/**
 * Unit tests for {@link ReconfigurationPlanExecutor}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanExecutorTest implements PremadeElements {

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
    public void test() throws InterruptedException, ReconfigurationPlanMonitorException {
        ReconfigurationPlan plan = makePlan();
        ReconfigurationPlanMonitor monitor = new DefaultReconfigurationPlanMonitor(plan);
        DummyExecutor de = new DummyExecutor();
        ReconfigurationPlanExecutor exec = new ReconfigurationPlanExecutor(monitor, de);
        try {
            exec.run();
            Assert.assertEquals(de.visited, plan.getSize());
        } catch (ReconfigurationPlanMonitorException ex) {
            System.err.println(ex.getMessage() + "\n" + ex.getAction() + "\n" + ex.getModel());
            throw ex;
        }
    }

    public static class DummyExecutor implements ActionVisitor {

        public int visited = 0;

        private Object go(Object a) {
            ++visited;
            try {
                Thread.sleep((long) Math.random() * 1000);
            } catch (InterruptedException ex) {
                Assert.fail(ex.getMessage(), ex);
            }
            return a;
        }

        @Override
        public Object visit(Allocate a) {
            return go(a);
        }

        @Override
        public Object visit(AllocateEvent a) {
            return go(a);
        }

        @Override
        public Object visit(BootNode a) {
            return go(a);
        }

        @Override
        public Object visit(BootVM a) {
            return go(a);
        }

        @Override
        public Object visit(ForgeVM a) {
            return go(a);
        }

        @Override
        public Object visit(KillVM a) {
            return go(a);
        }

        @Override
        public Object visit(MigrateVM a) {
            return go(a);
        }

        @Override
        public Object visit(ResumeVM a) {
            return go(a);
        }

        @Override
        public Object visit(ShutdownNode a) {
            return go(a);
        }

        @Override
        public Object visit(ShutdownVM a) {
            return go(a);
        }

        @Override
        public Object visit(SuspendVM a) {
            return go(a);
        }
    }
}
