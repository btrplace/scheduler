/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.model.*;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.MigrateVM;
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
        Assert.assertSame(p.getResult(), mo);
    }


    @Test
    public void testToString() {
        Model mo = new DefaultModel();
        VM v1 = mo.newVM();
        VM v2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addOnlineNode(n2);
        mo.getMapping().addRunningVM(v1, n1);
        mo.getMapping().addRunningVM(v2, n1);
        ReconfigurationPlan p1 = new DefaultReconfigurationPlan(mo);
        p1.add(new MigrateVM(v1, n1, n2, 1, 2));
        p1.add(new MigrateVM(v2, n1, n2, 1, 2));
        String s = p1.toString();
        //2 migrations
        Assert.assertNotEquals(s.indexOf("migrate("), s.lastIndexOf("migrate("));
        System.err.println(p1);
        System.err.flush();
    }


    @Test
    public void testInstantiate() {
        Model m = new DefaultModel();
        DefaultReconfigurationPlan p = new DefaultReconfigurationPlan(m);
        Assert.assertEquals(m, p.getOrigin());
        Assert.assertEquals(m, p.getResult());
        Assert.assertEquals(p.getDuration(), 0);
        Assert.assertTrue(p.getActions().isEmpty());
        Assert.assertFalse(p.toString().contains("null"));
        Assert.assertEquals(p.getReconfigurationApplier().getClass(), TimeBasedPlanApplier.class);

    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testAddDurationAndSize() {
        Model m = new DefaultModel();
        List<VM> vms = Util.newVMs(m, 10);
        DefaultReconfigurationPlan p = new DefaultReconfigurationPlan(m);
        Action a1 = new MockAction(vms.get(0), 1, 3);
        Action a2 = new MockAction(vms.get(1), 2, 4);
        Action a3 = new MockAction(vms.get(2), 2, 4);
        Action a4 = new MockAction(vms.get(3), 1, 3);
        Assert.assertTrue(p.add(a1));
        Assert.assertEquals(p.getDuration(), 3);
        Assert.assertTrue(p.add(a4));
        Assert.assertTrue(p.add(a3));
        Assert.assertTrue(p.add(a2));
        Assert.assertEquals(p.getDuration(), 4);
        int last = -1;
        System.out.println(p);
        for (Action a : p) {
            Assert.assertTrue(a.getStart() >= last);
            last = a.getStart();
        }
        Assert.assertFalse(p.add(a2));

        Assert.assertEquals(p.getSize(), 4);

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

        ReconfigurationPlan p2 = new DefaultReconfigurationPlan(mo.copy());
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
