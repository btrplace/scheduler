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

import org.btrplace.model.*;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.plan.event.SuspendVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit tests for {@link DefaultReconfigurationPlan}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlanTest {

    @Test
    public void testApplierGetAndSet() {
        Model m = new DefaultModel();
        ReconfigurationPlan p = new DefaultReconfigurationPlan(m);
        ReconfigurationPlanApplier ap = mock(ReconfigurationPlanApplier.class);
        p.setReconfigurationApplier(ap);
        Assert.assertEquals(p.getReconfigurationApplier(), ap);
    }

    @Test(dependsOnMethods = {"testApplierGetAndSet"})
    public void testApply() {
        Model m = new DefaultModel();
        ReconfigurationPlan p = new DefaultReconfigurationPlan(m);
        ReconfigurationPlanApplier ap = mock(ReconfigurationPlanApplier.class);
        p.setReconfigurationApplier(ap);

        Model mo = new DefaultModel();
        when(ap.apply(p)).thenReturn(mo);
        Assert.assertTrue(p.getResult() == mo);
    }

    @Test(dependsOnMethods = {"testApplierGetAndSet"})
    public void testToString() {
        Model m = new DefaultModel();
        ReconfigurationPlan p = new DefaultReconfigurationPlan(m);
        ReconfigurationPlanApplier ap = mock(ReconfigurationPlanApplier.class);
        p.setReconfigurationApplier(ap);

        when(ap.toString(p)).thenReturn("foo");
        Assert.assertEquals(p.toString(), "foo");
    }

    @Test
    public void testInstantiate() {
        Model m = new DefaultModel();
        DefaultReconfigurationPlan p = new DefaultReconfigurationPlan(m);
        Assert.assertEquals(m, p.getOrigin());
        Assert.assertEquals(m, p.getResult());
        Assert.assertEquals(0, p.getDuration());
        Assert.assertTrue(p.getActions().isEmpty());
        Assert.assertFalse(p.toString().contains("null"));
        Assert.assertEquals(p.getReconfigurationApplier().getClass(), TimeBasedPlanApplier.class);

    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testAddDurationAndSize() {
        Model m = new DefaultModel();
        List<VM> vms = Util.newVMs(m, 10);
        List<Node> ns = Util.newNodes(m, 10);
        DefaultReconfigurationPlan p = new DefaultReconfigurationPlan(m);
        Action a1 = new MockAction(vms.get(0), 1, 3);
        Action a2 = new MockAction(vms.get(1), 2, 4);
        Action a3 = new MockAction(vms.get(2), 2, 4);
        Action a4 = new MockAction(vms.get(3), 1, 3);
        Assert.assertTrue(p.add(a1));
        Assert.assertEquals(3, p.getDuration());
        Assert.assertTrue(p.add(a4));
        Assert.assertTrue(p.add(a3));
        Assert.assertTrue(p.add(a2));
        Assert.assertEquals(4, p.getDuration());
        int last = -1;
        System.out.println(p);
        for (Action a : p) {
            Assert.assertTrue(a.getStart() >= last);
            last = a.getStart();
        }
        Assert.assertFalse(p.add(a2));

        Assert.assertEquals(4, p.getSize());

        Assert.assertFalse(p.toString().contains("null"));
    }

    @Test
    public void testDumb() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addOnlineNode(n2);
        mo.getMapping().addRunningVM(v, n1);
        ReconfigurationPlan p1 = new DefaultReconfigurationPlan(mo);
        Action a1 = new SuspendVM(v, n1, n1, 0, 1);
        Action a2 = new ShutdownNode(n1, 1, 2);
        Action a3 = new ShutdownNode(n2, 1, 2);
        Assert.assertTrue(p1.add(a1));
        Assert.assertTrue(p1.add(a2));
        Assert.assertTrue(p1.add(a3));
        Assert.assertTrue(p1.getActions().contains(a1));
        Assert.assertTrue(p1.getActions().contains(a2));
        Assert.assertTrue(p1.getActions().contains(a3));
    }

    @Test
    public void testEquals() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addOnlineNode(n2);
        mo.getMapping().addRunningVM(v, n1);
        ReconfigurationPlan p1 = new DefaultReconfigurationPlan(mo);
        p1.add(new ShutdownNode(n1, 1, 2));
        p1.add(new ShutdownNode(n2, 1, 2));

        ReconfigurationPlan p2 = new DefaultReconfigurationPlan(mo.clone());
        p2.add(new ShutdownNode(n1, 1, 2));
        p2.add(new ShutdownNode(n2, 1, 2));

        Assert.assertEquals(p1, p2);
        /*
        java.lang.RuntimeException: The resulting schedule differ. Got:
0:1 {action=suspend(vm=vm#0, from=node#1, to=node#1)}
1:2 {action=shutdown(node=node#0)}
1:2 {action=shutdown(node=node#1)}

Expected:
0:1 {action=suspend(vm=vm#0, from=node#1, to=node#1)}
1:2 {action=shutdown(node=node#0)}
1:2 {action=shutdown(node=node#1)}
         */
    }
}
