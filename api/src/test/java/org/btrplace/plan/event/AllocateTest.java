/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;

import org.btrplace.model.*;
import org.btrplace.model.view.ShareableResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link Allocate}.
 *
 * @author Fabien Hermenier
 */
public class AllocateTest {

    static Model mo = new DefaultModel();
    static List<Node> ns = Util.newNodes(mo, 10);
    static List<VM> vms = Util.newVMs(mo, 10);
    static Allocate a = new Allocate(vms.get(0), ns.get(0), "foo", 3, 1, 5);

    @Test
    public void testInstantiation() {
        Allocate a = new Allocate(vms.get(0), ns.get(0), "foo", 3, 1, 5);
        Assert.assertEquals(vms.get(0), a.getVM());
        Assert.assertEquals(ns.get(0), a.getHost());
        Assert.assertEquals(a.getResourceId(), "foo");
        Assert.assertEquals(a.getAmount(), 3);
        Assert.assertEquals(a.getStart(), 1);
        Assert.assertEquals(a.getEnd(), 5);
        Assert.assertFalse(a.toString().contains("null"));
    }

    @Test
    public void testApply() {
        Model mo = new DefaultModel();
        Allocate na = new Allocate(vms.get(0), ns.get(1), "foo", 3, 3, 5);
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addRunningVM(vms.get(0), ns.get(0));
        Assert.assertFalse(na.apply(mo));
        ShareableResource rc = new ShareableResource("foo");
        mo.attach(rc);
        Assert.assertTrue(na.apply(mo));
        Assert.assertEquals(rc.getConsumption(vms.get(0)), 3);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEquals() {
        Allocate a = new Allocate(vms.get(0), ns.get(0), "foo", 5, 3, 5);
        Allocate b = new Allocate(vms.get(0), ns.get(0), "foo", 5, 3, 5);
        Assert.assertNotEquals(new Object(), a);
        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertNotSame(a, new Allocate(vms.get(2), ns.get(0), "foo", 5, 3, 5));
        Assert.assertNotSame(a, new Allocate(vms.get(0), ns.get(1), "foo", 5, 3, 5));
        Assert.assertNotSame(a, new Allocate(vms.get(0), ns.get(0), "bar", 5, 3, 5));
        Assert.assertNotSame(a, new Allocate(vms.get(0), ns.get(0), "foo", 6, 3, 5));
        Assert.assertNotSame(a, new Allocate(vms.get(0), ns.get(0), "foo", 5, 4, 5));
        Assert.assertNotSame(a, new Allocate(vms.get(0), ns.get(0), "foo", 5, 3, 7));
    }

    @Test
    public void testVisit() {
        ActionVisitor visitor = mock(ActionVisitor.class);
        a.visit(visitor);
        verify(visitor).visit(a);
    }

}
