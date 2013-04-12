package btrplace.plan;

import btrplace.model.Model;
import btrplace.plan.event.*;
import btrplace.test.PremadeElements;
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
    public void testListeners() {
        DefaultPlanApplier app = new MockApplier();
        EventCommittedListener ev = Mockito.mock(EventCommittedListener.class);
        app.addEventCommittedListener(ev);
        Assert.assertTrue(app.removeEventCommittedListener(ev));

    }

    @Test(dependsOnMethods = {"testListeners"})
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
