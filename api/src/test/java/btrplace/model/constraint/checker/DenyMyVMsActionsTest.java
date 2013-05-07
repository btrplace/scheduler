package btrplace.model.constraint.checker;

import btrplace.model.constraint.SatConstraint;
import btrplace.plan.event.*;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DenyMyVMsActions}.
 *
 * @author Fabien Hermenier
 */
public class DenyMyVMsActionsTest implements PremadeElements {

    static SatConstraint cstr = mock(SatConstraint.class);
    static Set<UUID> ns = new HashSet<>(Arrays.asList(n1, n2, n3));
    static Set<UUID> vs = new HashSet<>(Arrays.asList(vm1, vm2, vm3));

    @Test
    public void testInstantiation() {
        when(cstr.getInvolvedNodes()).thenReturn(ns);
        when(cstr.getInvolvedVMs()).thenReturn(vs);

        DenyMyVMsActions c = new DenyMyVMsActions(cstr) {
        };
        Assert.assertEquals(c.getConstraint(), cstr);
        Assert.assertEquals(c.getVMs(), vs);
        Assert.assertEquals(c.getNodes(), ns);
    }

    @Test
    public void testDeny() {
        when(cstr.getInvolvedNodes()).thenReturn(ns);
        when(cstr.getInvolvedVMs()).thenReturn(vs);

        DenyMyVMsActions c = new DenyMyVMsActions(cstr) {
        };

        Assert.assertFalse(c.start(new BootVM(vm1, n1, 0, 3)));
        Assert.assertTrue(c.start(new BootVM(vm9, n1, 0, 3)));
        Assert.assertFalse(c.start(new ResumeVM(vm1, n1, n2, 0, 3)));
        Assert.assertTrue(c.start(new ResumeVM(vm7, n1, n2, 0, 3)));
        Assert.assertFalse(c.start(new MigrateVM(vm1, n1, n2, 0, 3)));
        Assert.assertTrue(c.start(new MigrateVM(vm5, n1, n2, 0, 3)));


        Assert.assertFalse(c.start(new SuspendVM(vm1, n1, n2, 0, 3)));
        Assert.assertTrue(c.start(new SuspendVM(vm10, n1, n2, 0, 3)));

        Assert.assertFalse(c.start(new ShutdownVM(vm1, n1, 0, 3)));
        Assert.assertTrue(c.start(new ShutdownVM(vm6, n1, 0, 3)));

        Assert.assertFalse(c.start(new KillVM(vm1, n1, 0, 3)));
        Assert.assertTrue(c.start(new KillVM(vm7, n1, 0, 3)));

        Assert.assertFalse(c.start(new ForgeVM(vm1, 0, 3)));
        Assert.assertTrue(c.start(new ForgeVM(vm7, 0, 3)));

        Assert.assertFalse(c.start(new Allocate(vm1, n1, "cpu", 3, 4, 5)));
        Assert.assertTrue(c.start(new Allocate(vm6, n1, "cpu", 3, 4, 5)));

        Assert.assertFalse(c.consume(new SubstitutedVMEvent(vm1, vm3)));
        Assert.assertTrue(c.consume(new SubstitutedVMEvent(vm10, vm3)));

        Assert.assertFalse(c.consume(new AllocateEvent(vm3, "cpu", 3)));
        Assert.assertTrue(c.consume(new AllocateEvent(vm10, "cpu", 3)));
    }
}
