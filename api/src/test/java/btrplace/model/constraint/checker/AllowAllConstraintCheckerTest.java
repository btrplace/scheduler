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

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AllowAllConstraintChecker}.
 *
 * @author Fabien Hermenier
 */
public class AllowAllConstraintCheckerTest implements PremadeElements {

    static SatConstraint cstr = mock(SatConstraint.class);
    static Set<UUID> ns = new HashSet<>(Arrays.asList(n1, n2, n3));
    static Set<UUID> vs = new HashSet<>(Arrays.asList(vm1, vm2, vm3));


    @Test
    public void testInstantiation() {
        when(cstr.getInvolvedNodes()).thenReturn(ns);
        when(cstr.getInvolvedVMs()).thenReturn(vs);

        AllowAllConstraintChecker c = new AllowAllConstraintChecker(cstr) {
        };
        Assert.assertEquals(c.getConstraint(), cstr);
        Assert.assertEquals(c.getVMs(), vs);
        Assert.assertEquals(c.getNodes(), ns);
    }

    @Test
    public void testAcceptance() {
        AllowAllConstraintChecker c = mock(AllowAllConstraintChecker.class, CALLS_REAL_METHODS);

        MigrateVM m = new MigrateVM(vm1, n1, n2, 0, 3);
        Assert.assertTrue(c.start(m));
        verify(c).startRunningVMPlacement(m);
        c.end(m);
        verify(c).endRunningVMPlacement(m);

        BootVM b = new BootVM(vm1, n1, 0, 3);
        Assert.assertTrue(c.start(b));
        verify(c).startRunningVMPlacement(b);
        c.end(b);
        verify(c).endRunningVMPlacement(b);


        ResumeVM r = new ResumeVM(vm1, n1, n2, 0, 3);
        Assert.assertTrue(c.start(r));
        verify(c).startRunningVMPlacement(r);
        c.end(r);
        verify(c).endRunningVMPlacement(r);

        //do not use the mock as the constructor is important
        //while earlier, the mock was needed for the verify()
        c = new AllowAllConstraintChecker(cstr) {
        };

        SuspendVM s = new SuspendVM(vm1, n1, n2, 0, 3);
        Assert.assertTrue(c.start(s));

        ShutdownVM s2 = new ShutdownVM(vm1, n1, 0, 3);
        Assert.assertTrue(c.start(s2));

        KillVM k = new KillVM(vm1, n1, 0, 3);
        Assert.assertTrue(c.start(k));

        ForgeVM f = new ForgeVM(vm1, 0, 3);
        Assert.assertTrue(c.start(f));

        BootNode bn = new BootNode(n1, 0, 3);
        Assert.assertTrue(c.start(bn));

        ShutdownNode sn = new ShutdownNode(n1, 0, 3);
        Assert.assertTrue(c.start(sn));

        SubstitutedVMEvent ss = new SubstitutedVMEvent(vm1, vm3);
        Assert.assertTrue(c.consume(ss));

        Allocate a = new Allocate(vm1, n1, "cpu", 3, 4, 5);
        Assert.assertTrue(c.start(a));

        AllocateEvent ae = new AllocateEvent(vm1, "cpu", 3);
        Assert.assertTrue(c.consume(ae));
    }

    @Test(dependsOnMethods = "testInstantiation")
    public void testMyVMsTracking() {

        when(cstr.getInvolvedNodes()).thenReturn(ns);
        when(cstr.getInvolvedVMs()).thenReturn(vs);

        AllowAllConstraintChecker c = new AllowAllConstraintChecker(cstr) {
        };

        //VM1 (one of the involved vms) has to be removed to be substituted by vm10
        c.consume(new SubstitutedVMEvent(vm1, vm10));
        Assert.assertTrue(c.getVMs().contains(vm10));
        Assert.assertFalse(c.getVMs().contains(vm1));

        //VM5 is not involved, no removal
        c.consume(new SubstitutedVMEvent(vm5, vm7));
        Assert.assertFalse(c.getVMs().contains(vm5));
        Assert.assertFalse(c.getVMs().contains(vm7));
    }

    @Test(dependsOnMethods = "testInstantiation")
    public void testAnyTracking() {

        AllowAllConstraintChecker c = new AllowAllConstraintChecker(cstr) {
        };

        Set<UUID> vms = new HashSet<>(Arrays.asList(vm5, vm7, vm9));
        c.track(vms);
        //VM1 (one of the involved vms) has to be removed to be substituted by vm10
        c.consume(new SubstitutedVMEvent(vm7, vm10));
        Assert.assertTrue(vms.contains(vm10));
        Assert.assertFalse(vms.contains(vm7));

        //VM5 is not involved, no removal
        c.consume(new SubstitutedVMEvent(vm7, vm1));
        Assert.assertFalse(vms.contains(vm7));
        Assert.assertFalse(vms.contains(vm1));
    }
}
