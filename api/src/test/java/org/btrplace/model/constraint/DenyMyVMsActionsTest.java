/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.Util;
import org.btrplace.model.VM;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.AllocateEvent;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.ForgeVM;
import org.btrplace.plan.event.KillVM;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ResumeVM;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SubstitutedVMEvent;
import org.btrplace.plan.event.SuspendVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link org.btrplace.model.constraint.DenyMyVMsActions}.
 *
 * @author Fabien Hermenier
 */
public class DenyMyVMsActionsTest {

    static SatConstraint cstr = mock(SatConstraint.class);

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 10);
        List<Node> ns = Util.newNodes(mo, 10);

        when(cstr.getInvolvedNodes()).thenReturn(ns);
        when(cstr.getInvolvedVMs()).thenReturn(vms);

        DenyMyVMsActions<SatConstraint> c = new DenyMyVMsActions<SatConstraint>(cstr) {
        };
        Assert.assertEquals(c.getConstraint(), cstr);
        Assert.assertEquals(c.getVMs(), vms);
        Assert.assertEquals(c.getNodes(), ns);
    }

    @Test
    public void testDeny() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 10);
        List<Node> ns = Util.newNodes(mo, 10);

        when(cstr.getInvolvedNodes()).thenReturn(Arrays.asList(ns.get(0), ns.get(1), ns.get(2)));
        when(cstr.getInvolvedVMs()).thenReturn(Arrays.asList(vms.get(0), vms.get(1), vms.get(2)));

        DenyMyVMsActions<SatConstraint> c = new DenyMyVMsActions<SatConstraint>(cstr) {
        };

        Assert.assertFalse(c.start(new BootVM(vms.get(0), ns.get(0), 0, 3)));
        Assert.assertTrue(c.start(new BootVM(vms.get(8), ns.get(0), 0, 3)));
        Assert.assertFalse(c.start(new ResumeVM(vms.get(0), ns.get(0), ns.get(1), 0, 3)));
        Assert.assertTrue(c.start(new ResumeVM(vms.get(6), ns.get(0), ns.get(1), 0, 3)));
        Assert.assertFalse(c.start(new MigrateVM(vms.get(0), ns.get(0), ns.get(1), 0, 3)));
        Assert.assertTrue(c.start(new MigrateVM(vms.get(4), ns.get(0), ns.get(1), 0, 3)));


        Assert.assertFalse(c.start(new SuspendVM(vms.get(0), ns.get(0), ns.get(1), 0, 3)));
        Assert.assertTrue(c.start(new SuspendVM(vms.get(9), ns.get(0), ns.get(1), 0, 3)));

        Assert.assertFalse(c.start(new ShutdownVM(vms.get(0), ns.get(0), 0, 3)));
        Assert.assertTrue(c.start(new ShutdownVM(vms.get(5), ns.get(0), 0, 3)));

        Assert.assertFalse(c.start(new KillVM(vms.get(0), ns.get(0), 0, 3)));
        Assert.assertTrue(c.start(new KillVM(vms.get(6), ns.get(0), 0, 3)));

        Assert.assertFalse(c.start(new ForgeVM(vms.get(0), 0, 3)));
        Assert.assertTrue(c.start(new ForgeVM(vms.get(6), 0, 3)));

        Assert.assertFalse(c.start(new Allocate(vms.get(0), ns.get(0), "cpu", 3, 4, 5)));
        Assert.assertTrue(c.start(new Allocate(vms.get(5), ns.get(0), "cpu", 3, 4, 5)));

        Assert.assertFalse(c.consume(new SubstitutedVMEvent(vms.get(0), vms.get(2))));
        Assert.assertTrue(c.consume(new SubstitutedVMEvent(vms.get(9), vms.get(2))));

        Assert.assertFalse(c.consume(new AllocateEvent(vms.get(2), "cpu", 3)));
        Assert.assertTrue(c.consume(new AllocateEvent(vms.get(9), "cpu", 3)));
    }
}
