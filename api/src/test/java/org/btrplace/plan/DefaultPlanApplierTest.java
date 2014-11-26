/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.plan;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.Util;
import org.btrplace.model.VM;
import org.btrplace.plan.event.*;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultPlanApplier}.
 *
 * @author Fabien Hermenier
 */
public class DefaultPlanApplierTest {

    static List<VM> vms = Util.newVMs(10);
    static List<Node> ns = Util.newNodes(10);

    @Test
    public void testEventCommittedListeners() {
        DefaultPlanApplier app = new MockApplier();
        EventCommittedListener ev = Mockito.mock(EventCommittedListener.class);
        app.addEventCommittedListener(ev);
        Assert.assertTrue(app.removeEventCommittedListener(ev));
    }

    @Test(dependsOnMethods = {"testEventCommittedListeners"})
    public void testFireSimpleAction() {
        DefaultPlanApplier app = new MockApplier();
        EventCommittedListener ev = mock(EventCommittedListener.class);
        app.addEventCommittedListener(ev);

        BootVM b = new BootVM(vms.get(0), ns.get(0), 0, 5);
        app.fireAction(b);
        verify(ev, times(1)).committed(b);

        ShutdownVM svm = new ShutdownVM(vms.get(0), ns.get(0), 0, 5);
        app.fireAction(svm);
        verify(ev, times(1)).committed(svm);

        BootNode bn = new BootNode(ns.get(0), 0, 5);
        app.fireAction(bn);
        verify(ev, times(1)).committed(bn);

        ShutdownNode sn = new ShutdownNode(ns.get(0), 0, 5);
        app.fireAction(sn);
        verify(ev, times(1)).committed(sn);

        SuspendVM susVM = new SuspendVM(vms.get(0), ns.get(0), ns.get(1), 0, 5);
        app.fireAction(susVM);
        verify(ev, times(1)).committed(susVM);

        ResumeVM resVM = new ResumeVM(vms.get(0), ns.get(0), ns.get(1), 0, 5);
        app.fireAction(resVM);
        verify(ev, times(1)).committed(resVM);

        MigrateVM miVM = new MigrateVM(vms.get(0), ns.get(0), ns.get(1), 0, 5);
        app.fireAction(miVM);
        verify(ev, times(1)).committed(miVM);

        KillVM kvm = new KillVM(vms.get(0), ns.get(0), 0, 5);
        app.fireAction(kvm);
        verify(ev, times(1)).committed(kvm);

        ForgeVM fvm = new ForgeVM(vms.get(0), 0, 5);
        app.fireAction(fvm);
        verify(ev, times(1)).committed(fvm);
    }

    @Test
    public void testFireComposedAction() {
        DefaultPlanApplier app = new MockApplier();
        EventCommittedListener ev = mock(EventCommittedListener.class);
        app.addEventCommittedListener(ev);

        BootVM b = new BootVM(vms.get(0), ns.get(0), 0, 5);
        AllocateEvent pre = new AllocateEvent(vms.get(0), "cpu", 7);
        b.addEvent(Action.Hook.PRE, pre);
        SubstitutedVMEvent post = new SubstitutedVMEvent(vms.get(0), vms.get(3));
        b.addEvent(Action.Hook.POST, post);

        InOrder order = inOrder(ev);
        app.fireAction(b);
        order.verify(ev).committed(pre);
        order.verify(ev).committed(b);
        order.verify(ev).committed(post);
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
