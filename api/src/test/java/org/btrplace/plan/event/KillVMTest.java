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
 * Unit tests for {@link KillVM}.
 *
 * @author Fabien Hermenier
 */
public class KillVMTest {

    static Model mo = new DefaultModel();
    static List<Node> ns = Util.newNodes(mo, 10);
    static List<VM> vms = Util.newVMs(mo, 10);
    static KillVM a = new KillVM(vms.get(0), ns.get(0), 3, 5);

    @Test
    public void testInstantiate() {
        KillVM a = new KillVM(vms.get(0), ns.get(0), 3, 5);
        Assert.assertEquals(vms.get(0), a.getVM());
        Assert.assertEquals(ns.get(0), a.getNode());
        Assert.assertEquals(a.getStart(), 3);
        Assert.assertEquals(a.getEnd(), 5);
        Assert.assertFalse(a.toString().contains("null"));
        Assert.assertEquals(a.getCurrentState(), VMState.RUNNING);
        Assert.assertEquals(a.getNextState(), VMState.KILLED);
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testApply() {
        Model m = new DefaultModel();
        Mapping map = m.getMapping();
        KillVM a = new KillVM(vms.get(0), ns.get(0), 3, 5);
        map.addOnlineNode(ns.get(0));
        map.addRunningVM(vms.get(0), ns.get(0));
        Assert.assertTrue(a.apply(m));
        Assert.assertFalse(map.contains(vms.get(0)));

        Assert.assertFalse(a.apply(m));

        map.addSleepingVM(vms.get(0), ns.get(0));
        Assert.assertTrue(a.apply(m));

        map.addReadyVM(vms.get(0));
        Assert.assertTrue(a.apply(m));
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testEquals() {
        KillVM a = new KillVM(vms.get(0), ns.get(0), 3, 5);
        KillVM b = new KillVM(vms.get(0), ns.get(0), 3, 5);
        Assert.assertNotEquals(new Object(), a);
        Assert.assertEquals(a, a);
        Assert.assertEquals(b, a);
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertNotSame(a, new KillVM(vms.get(0), ns.get(0), 4, 5));
        Assert.assertNotSame(a, new KillVM(vms.get(0), ns.get(0), 3, 4));
        Assert.assertNotSame(a, new KillVM(vms.get(0), ns.get(1), 3, 5));
        Assert.assertNotSame(a, new KillVM(vms.get(1), ns.get(0), 4, 5));
    }

    @Test
    public void testVisit() {
        ActionVisitor visitor = mock(ActionVisitor.class);
        a.visit(visitor);
        verify(visitor).visit(a);
    }
}
