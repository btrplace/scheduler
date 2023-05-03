/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;

import org.btrplace.model.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ShutdownNode}.
 *
 * @author Fabien Hermenier
 */
public class ShutdownNodeTest {

    static Model mo = new DefaultModel();
    static List<Node> ns = Util.newNodes(mo, 10);
    static List<VM> vms = Util.newVMs(mo, 10);
    static ShutdownNode a = new ShutdownNode(ns.get(0), 3, 5);

    @Test
    public void testInstantiate() {
        ShutdownNode a = new ShutdownNode(ns.get(0), 3, 5);
        Assert.assertEquals(ns.get(0), a.getNode());
        Assert.assertEquals(a.getStart(), 3);
        Assert.assertEquals(a.getEnd(), 5);
        Assert.assertFalse(a.toString().contains("null"));
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testApply() {
        Model m = new DefaultModel();
        Mapping map = m.getMapping();
        ShutdownNode a = new ShutdownNode(ns.get(0), 3, 5);
        map.addOnlineNode(ns.get(0));
        Assert.assertTrue(a.apply(m));
        Assert.assertTrue(map.isOffline(ns.get(0)));

        Assert.assertFalse(a.apply(m));

        map.addOnlineNode(ns.get(0));
        map.addRunningVM(vms.get(0), ns.get(0));
        Assert.assertFalse(a.apply(m));
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testEquals() {
        ShutdownNode a = new ShutdownNode(ns.get(0), 3, 5);
        ShutdownNode b = new ShutdownNode(ns.get(0), 3, 5);
        Assert.assertNotEquals(new Object(), a);
        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertNotSame(a, new ShutdownNode(ns.get(0), 4, 5));
        Assert.assertNotSame(a, new ShutdownNode(ns.get(0), 3, 4));
        Assert.assertNotSame(a, new ShutdownNode(ns.get(1), 4, 5));
    }

    @Test
    public void testVisit() {
        ActionVisitor visitor = mock(ActionVisitor.class);
        a.visit(visitor);
        verify(visitor).visit(a);
    }

}
