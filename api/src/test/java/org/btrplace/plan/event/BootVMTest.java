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
import org.btrplace.model.VMState;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link BootNode}.
 *
 * @author Fabien Hermenier
 */
public class BootVMTest {

    static Model mo = new DefaultModel();
    static List<Node> ns = Util.newNodes(mo, 10);
    static List<VM> vms = Util.newVMs(mo, 10);
    static BootVM a = new BootVM(vms.get(0), ns.get(0), 3, 5);

    @Test
    public void testInstantiate() {
        BootVM a = new BootVM(vms.get(0), ns.get(0), 3, 5);
        Assert.assertEquals(vms.get(0), a.getVM());
        Assert.assertEquals(ns.get(0), a.getDestinationNode());
        Assert.assertEquals(3, a.getStart());
        Assert.assertEquals(5, a.getEnd());
        Assert.assertFalse(a.toString().contains("null"));
        Assert.assertEquals(a.getCurrentState(), VMState.READY);
        Assert.assertEquals(a.getNextState(), VMState.RUNNING);

    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testApply() {
        Model m = new DefaultModel();
        Mapping map = m.getMapping();
        BootVM a = new BootVM(vms.get(0), ns.get(0), 3, 5);
        map.addOnlineNode(ns.get(0));
        map.addReadyVM(vms.get(0));
        Assert.assertTrue(a.apply(m));
        Assert.assertTrue(map.isRunning(vms.get(0)));
        Assert.assertEquals(map.getVMLocation(vms.get(0)), ns.get(0));

        Assert.assertFalse(a.apply(m));

        map.addSleepingVM(vms.get(0), ns.get(0));
        Assert.assertFalse(a.apply(m));
        Assert.assertTrue(map.isSleeping(vms.get(0)));

        map.remove(vms.get(0));
        Assert.assertFalse(a.apply(m));

        map.addReadyVM(vms.get(0));
        map.addOfflineNode(ns.get(0));
        Assert.assertFalse(a.apply(m));
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testEquals() {
        BootVM a = new BootVM(vms.get(0), ns.get(0), 3, 5);
        BootVM b = new BootVM(vms.get(0), ns.get(0), 3, 5);
        Assert.assertFalse(a.equals(new Object()));
        Assert.assertTrue(a.equals(a));
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertNotSame(a, new BootVM(vms.get(0), ns.get(0), 4, 5));
        Assert.assertNotSame(a, new BootVM(vms.get(0), ns.get(0), 3, 4));
        Assert.assertNotSame(a, new BootVM(vms.get(0), ns.get(1), 3, 5));
        Assert.assertNotSame(a, new BootVM(vms.get(1), ns.get(0), 4, 5));
    }

    @Test
    public void testVisit() {
        ActionVisitor visitor = mock(ActionVisitor.class);
        a.visit(visitor);
        verify(visitor).visit(a);
    }
}
