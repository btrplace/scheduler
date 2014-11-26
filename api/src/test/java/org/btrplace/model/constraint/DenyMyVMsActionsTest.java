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

package org.btrplace.model.constraint;

import org.btrplace.model.*;
import org.btrplace.plan.event.*;
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

        DenyMyVMsActions c = new DenyMyVMsActions(cstr) {
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

        DenyMyVMsActions c = new DenyMyVMsActions(cstr) {
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
