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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link org.btrplace.model.constraint.AllowAllConstraintChecker}.
 *
 * @author Fabien Hermenier
 */
public class AllowAllConstraintCheckerTest {


    @Test
    public void testInstantiation() {
        SatConstraint cstr = mock(SatConstraint.class);
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 10);
        List<Node> ns = Util.newNodes(mo, 10);

        when(cstr.getInvolvedNodes()).thenReturn(ns);
        when(cstr.getInvolvedVMs()).thenReturn(vms);

        AllowAllConstraintChecker c = new AllowAllConstraintChecker(cstr) {
        };
        Assert.assertEquals(c.getConstraint(), cstr);
        Assert.assertEquals(c.getVMs(), vms);
        Assert.assertEquals(c.getNodes(), ns);
    }

    @Test
    public void testAcceptance() {
        SatConstraint cstr = mock(SatConstraint.class);
        AllowAllConstraintChecker c = mock(AllowAllConstraintChecker.class, CALLS_REAL_METHODS);

        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 10);
        List<Node> ns = Util.newNodes(mo, 10);
        when(cstr.getInvolvedNodes()).thenReturn(ns);
        when(cstr.getInvolvedVMs()).thenReturn(vms);

        MigrateVM m = new MigrateVM(vms.get(0), ns.get(0), ns.get(1), 0, 3);
        Assert.assertTrue(c.start(m));
        verify(c).startRunningVMPlacement(m);
        c.end(m);
        verify(c).endRunningVMPlacement(m);

        BootVM b = new BootVM(vms.get(0), ns.get(0), 0, 3);
        Assert.assertTrue(c.start(b));
        verify(c).startRunningVMPlacement(b);
        c.end(b);
        verify(c).endRunningVMPlacement(b);


        ResumeVM r = new ResumeVM(vms.get(0), ns.get(0), ns.get(1), 0, 3);
        Assert.assertTrue(c.start(r));
        verify(c).startRunningVMPlacement(r);
        c.end(r);
        verify(c).endRunningVMPlacement(r);

        //do not use the mock as the constructor is important
        //while earlier, the mock was needed for the verify()
        c = new AllowAllConstraintChecker(cstr) {
        };
        Set<VM> allVMs = new HashSet<>();
        for (Node n : mo.getMapping().getOnlineNodes()) {
            allVMs.addAll(mo.getMapping().getRunningVMs(n));
            allVMs.addAll(mo.getMapping().getSleepingVMs(n));
        }
        allVMs.addAll(mo.getMapping().getReadyVMs());
        c.track(allVMs);
        SuspendVM s = new SuspendVM(vms.get(0), ns.get(0), ns.get(1), 0, 3);
        Assert.assertTrue(c.start(s));

        ShutdownVM s2 = new ShutdownVM(vms.get(0), ns.get(0), 0, 3);
        Assert.assertTrue(c.start(s2));

        KillVM k = new KillVM(vms.get(0), ns.get(0), 0, 3);
        Assert.assertTrue(c.start(k));

        ForgeVM f = new ForgeVM(vms.get(0), 0, 3);
        Assert.assertTrue(c.start(f));

        BootNode bn = new BootNode(ns.get(0), 0, 3);
        Assert.assertTrue(c.start(bn));

        ShutdownNode sn = new ShutdownNode(ns.get(0), 0, 3);
        Assert.assertTrue(c.start(sn));

        SubstitutedVMEvent ss = new SubstitutedVMEvent(vms.get(9), vms.get(2));
        Assert.assertTrue(c.consume(ss));

        Allocate a = new Allocate(vms.get(0), ns.get(0), "cpu", 3, 4, 5);
        Assert.assertTrue(c.start(a));

        AllocateEvent ae = new AllocateEvent(vms.get(0), "cpu", 3);
        Assert.assertTrue(c.consume(ae));
    }

    @Test(dependsOnMethods = "testInstantiation")
    public void testMyVMsTracking() {
        SatConstraint cstr = mock(SatConstraint.class);
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 10);
        List<Node> ns = Util.newNodes(mo, 10);
        when(cstr.getInvolvedNodes()).thenReturn(ns);
        when(cstr.getInvolvedVMs()).thenReturn(vms);

        AllowAllConstraintChecker c = new AllowAllConstraintChecker(cstr) {
        };

        //VM1 (one of the involved vms) has to be removed to be substituted by vms.get(0)0
        c.consume(new SubstitutedVMEvent(vms.get(0), vms.get(9)));
        Assert.assertTrue(c.getVMs().contains(vms.get(9)));
        Assert.assertFalse(c.getVMs().contains(vms.get(0)));

        //VM5 is not involved, no removal
        VM v = mo.newVM();
        c.consume(new SubstitutedVMEvent(vms.get(0), v));
        Assert.assertFalse(c.getVMs().contains(vms.get(0)));
        Assert.assertFalse(c.getVMs().contains(v));
    }

    @Test(dependsOnMethods = "testInstantiation")
    public void testAnyTracking() {
        SatConstraint cstr = mock(SatConstraint.class);
        AllowAllConstraintChecker c = new AllowAllConstraintChecker(cstr) {
        };

        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 10);
        when(cstr.getInvolvedVMs()).thenReturn(vms);

        Set<VM> vs = new HashSet<>(Arrays.asList(vms.get(4), vms.get(6), vms.get(9)));
        c.track(vs);
        //VM1 (one of the involved vms) has to be removed to be substituted by vms.get(0)0
        c.consume(new SubstitutedVMEvent(vms.get(6), vms.get(9)));
        Assert.assertTrue(vs.contains(vms.get(9)));
        Assert.assertFalse(vs.contains(vms.get(6)));

        //VM5 is not involved, no removal
        c.consume(new SubstitutedVMEvent(vms.get(6), vms.get(0)));
        Assert.assertFalse(vs.contains(vms.get(6)));
        Assert.assertFalse(vs.contains(vms.get(0)));
    }
}
