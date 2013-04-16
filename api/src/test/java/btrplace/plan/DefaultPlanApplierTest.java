package btrplace.plan;

import btrplace.model.Model;
import btrplace.plan.event.*;
import btrplace.test.PremadeElements;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultPlanApplier}.
 *
 * @author Fabien Hermenier
 */
public class DefaultPlanApplierTest implements PremadeElements {

    @Test
    public void testEventCommittedListeners() {
        DefaultPlanApplier app = new MockApplier();
        EventCommittedListener ev = Mockito.mock(EventCommittedListener.class);
        app.addEventCommittedListener(ev);
        Assert.assertTrue(app.removeEventCommittedListener(ev));
    }

    @Test
    public void testValidators() {
        DefaultPlanApplier app = new MockApplier();
        ReconfigurationPlanValidator ev = Mockito.mock(ReconfigurationPlanValidator.class);
        app.addValidator(ev);
        Assert.assertTrue(app.removeValidator(ev));
    }

    @Test(dependsOnMethods = {"testEventCommittedListeners"})
    public void testFireSimpleAction() {
        DefaultPlanApplier app = new MockApplier();
        EventCommittedListener ev = mock(EventCommittedListener.class);
        app.addEventCommittedListener(ev);

        BootVM b = new BootVM(vm1, n1, 0, 5);
        app.fireAction(b);
        verify(ev, times(1)).committed(b);

        ShutdownVM svm = new ShutdownVM(vm1, n1, 0, 5);
        app.fireAction(svm);
        verify(ev, times(1)).committed(svm);

        BootNode bn = new BootNode(n1, 0, 5);
        app.fireAction(bn);
        verify(ev, times(1)).committed(bn);

        ShutdownNode sn = new ShutdownNode(n1, 0, 5);
        app.fireAction(sn);
        verify(ev, times(1)).committed(sn);

        SuspendVM susVM = new SuspendVM(vm1, n1, n2, 0, 5);
        app.fireAction(susVM);
        verify(ev, times(1)).committed(susVM);

        ResumeVM resVM = new ResumeVM(vm1, n1, n2, 0, 5);
        app.fireAction(resVM);
        verify(ev, times(1)).committed(resVM);

        MigrateVM miVM = new MigrateVM(vm1, n1, n2, 0, 5);
        app.fireAction(miVM);
        verify(ev, times(1)).committed(miVM);

        KillVM kvm = new KillVM(vm1, n1, 0, 5);
        app.fireAction(kvm);
        verify(ev, times(1)).committed(kvm);

        ForgeVM fvm = new ForgeVM(vm1, 0, 5);
        app.fireAction(fvm);
        verify(ev, times(1)).committed(fvm);
    }

    @Test(dependsOnMethods = {"testEventCommittedListeners", "testValidators"})
    public void testFireUnvalidatedAction() {
        DefaultPlanApplier app = new MockApplier();
        EventCommittedListener ev = mock(EventCommittedListener.class);
        ReconfigurationPlanValidator v = mock(ReconfigurationPlanValidator.class);

        app.addEventCommittedListener(ev);
        app.addValidator(v);

        BootVM b = new BootVM(vm1, n1, 0, 5);
        when(v.accept(b)).thenReturn(false);
        app.fireAction(b);
        verify(v, times(1)).accept(b);
        verify(ev, times(0)).committed(b);

        ShutdownVM svm = new ShutdownVM(vm1, n1, 0, 5);
        when(v.accept(svm)).thenReturn(false);
        app.fireAction(svm);
        verify(v, times(1)).accept(svm);
        verify(ev, times(0)).committed(svm);


        BootNode bn = new BootNode(n1, 0, 5);
        when(v.accept(bn)).thenReturn(false);
        app.fireAction(bn);
        verify(v, times(1)).accept(bn);
        verify(ev, times(0)).committed(bn);

        ShutdownNode sn = new ShutdownNode(n1, 0, 5);
        when(v.accept(sn)).thenReturn(false);
        app.fireAction(sn);
        verify(v, times(1)).accept(sn);
        verify(ev, times(0)).committed(sn);

        SuspendVM susVM = new SuspendVM(vm1, n1, n2, 0, 5);
        when(v.accept(susVM)).thenReturn(false);
        app.fireAction(susVM);
        verify(v, times(1)).accept(susVM);
        verify(ev, times(0)).committed(susVM);

        ResumeVM resVM = new ResumeVM(vm1, n1, n2, 0, 5);
        when(v.accept(resVM)).thenReturn(false);
        app.fireAction(resVM);
        verify(v, times(1)).accept(resVM);
        verify(ev, times(0)).committed(resVM);


        MigrateVM miVM = new MigrateVM(vm1, n1, n2, 0, 5);
        when(v.accept(miVM)).thenReturn(false);
        app.fireAction(miVM);
        verify(v, times(1)).accept(miVM);
        verify(ev, times(0)).committed(miVM);


        KillVM kvm = new KillVM(vm1, n1, 0, 5);
        when(v.accept(kvm)).thenReturn(false);
        app.fireAction(kvm);
        verify(v, times(1)).accept(kvm);
        verify(ev, times(0)).committed(kvm);


        ForgeVM fvm = new ForgeVM(vm1, 0, 5);
        when(v.accept(fvm)).thenReturn(false);
        app.fireAction(fvm);
        verify(v, times(1)).accept(fvm);
        verify(ev, times(0)).committed(fvm);
    }

    @Test
    public void testFireComposedAction() {
        DefaultPlanApplier app = new MockApplier();
        EventCommittedListener ev = mock(EventCommittedListener.class);
        app.addEventCommittedListener(ev);

        BootVM b = new BootVM(vm1, n1, 0, 5);
        AllocateEvent pre = new AllocateEvent(vm1, "cpu", 7);
        b.addEvent(Action.Hook.pre, pre);
        SubstitutedVMEvent post = new SubstitutedVMEvent(vm1, vm4);
        b.addEvent(Action.Hook.post, post);

        InOrder order = inOrder(ev);
        app.fireAction(b);
        order.verify(ev).committed(pre);
        order.verify(ev).committed(b);
        order.verify(ev).committed(post);
    }


    @Test(dependsOnMethods = {"testFireComposedAction"})
    public void testFireDeniedComposedAction() {
        DefaultPlanApplier app = new MockApplier();
        EventCommittedListener ev = mock(EventCommittedListener.class);
        ReconfigurationPlanValidator v = mock(ReconfigurationPlanValidator.class);

        app.addEventCommittedListener(ev);
        app.addValidator(v);

        BootVM b = new BootVM(vm1, n1, 0, 5);
        AllocateEvent pre = new AllocateEvent(vm1, "cpu", 7);
        b.addEvent(Action.Hook.pre, pre);
        SubstitutedVMEvent post = new SubstitutedVMEvent(vm1, vm4);
        b.addEvent(Action.Hook.post, post);

        when(v.accept(b)).thenReturn(false);

        app.fireAction(b);
        verify(v).accept(pre);
        verify(v, times(0)).accept(b);
        verify(v, times(0)).accept(post);
    }

    class MockApplier extends DefaultPlanApplier {
        @Override
        public Model apply(ReconfigurationPlan p) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString(ReconfigurationPlan p) {
            throw new UnsupportedOperationException();
        }
    }

}
