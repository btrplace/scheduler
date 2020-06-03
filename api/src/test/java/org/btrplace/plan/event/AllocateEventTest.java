/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.Util;
import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link AllocateEvent}.
 *
 * @author Fabien Hermenier
 */
public class AllocateEventTest {

    static Model mo = new DefaultModel();
    static List<Node> ns = Util.newNodes(mo, 10);
    static List<VM> vms = Util.newVMs(mo, 10);
    static AllocateEvent a = new AllocateEvent(vms.get(0), "foo", 3);

    @Test
    public void testBasics() {
        AllocateEvent na = new AllocateEvent(vms.get(0), "foo", 3);
        Assert.assertEquals(vms.get(0), na.getVM());
        Assert.assertEquals("foo", na.getResourceId());
        Assert.assertEquals(3, na.getAmount());
        Assert.assertFalse(na.toString().contains("null"));

    }

    @Test
    public void testEqualsHashCode() {
        AllocateEvent na = new AllocateEvent(vms.get(0), "foo", 3);
        AllocateEvent na2 = new AllocateEvent(vms.get(0), "foo", 3);
        Assert.assertFalse(na.equals(new Object()));
        Assert.assertTrue(na.equals(na));
        Assert.assertTrue(na.equals(na2));
        Assert.assertTrue(na2.equals(na));
        Assert.assertEquals(na.hashCode(), na2.hashCode());
        Assert.assertFalse(na.equals(new AllocateEvent(vms.get(1), "foo", 3)));
        Assert.assertFalse(na.equals(new AllocateEvent(vms.get(0), "bar", 3)));
        Assert.assertFalse(na.equals(new AllocateEvent(vms.get(0), "foo", 5)));
    }

    @Test
    public void testApply() {
        AllocateEvent na = new AllocateEvent(vms.get(0), "foo", 3);
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addRunningVM(vms.get(0), ns.get(0));
        Assert.assertFalse(na.apply(mo));
        ShareableResource rc = new ShareableResource("foo");
        mo.attach(rc);
        Assert.assertTrue(na.apply(mo));
        Assert.assertEquals(3, rc.getConsumption(vms.get(0)));
    }

    @Test
    public void testVisit() {
        ActionVisitor visitor = mock(ActionVisitor.class);
        a.visit(visitor);
        verify(visitor).visit(a);
    }
}
