/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.Util;
import org.btrplace.model.VM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link Fence}.
 *
 * @author Fabien Hermenier
 */
public class FenceTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);

        Set<Node> nodes = new HashSet<>(Collections.singletonList(ns.get(0)));
        Fence f = new Fence(vms.get(0), nodes);
        Assert.assertNotNull(f.getChecker());
        Assert.assertEquals(vms.get(0), f.getInvolvedVMs().iterator().next());
        Assert.assertEquals(nodes, f.getInvolvedNodes());
        Assert.assertFalse(f.toString().contains("null"));
        Assert.assertFalse(f.isContinuous());
        Assert.assertTrue(f.setContinuous(true));
        System.out.println(f);
    }

    @Test
    public void testIsSatisfied() {
        Model m = new DefaultModel();
        List<Node> ns = Util.newNodes(m, 10);
        List<VM> vms = Util.newVMs(m, 10);

        Mapping map = m.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(1));
        map.addRunningVM(vms.get(2), ns.get(1));

        Set<Node> nodes = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));

        Fence f = new Fence(vms.get(2), nodes);
        Assert.assertEquals(true, f.isSatisfied(m));
        map.addRunningVM(vms.get(0), ns.get(2));
        Assert.assertEquals(false, new Fence(vms.get(0), nodes).isSatisfied(m));
    }

    @Test
    public void testEquals() {

        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Set<Node> nodes = new HashSet<>(Arrays.asList(mo.newNode(), mo.newNode()));
        Fence f = new Fence(v, nodes);
        Assert.assertTrue(f.equals(f));
        Assert.assertTrue(new Fence(v, nodes).equals(f));
        Assert.assertFalse(new Fence(mo.newVM(), nodes).equals(f));
        Assert.assertEquals(new Fence(v, nodes).hashCode(), f.hashCode());
    }

    @Test
    public void testFences() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 5);
        List<Node> ns = Util.newNodes(mo, 5);
        List<Fence> c = Fence.newFence(vms, ns);
        Assert.assertEquals(vms.size(), c.size());
        c.stream().forEach((q) -> {
            Assert.assertTrue(vms.containsAll(q.getInvolvedVMs()));
            Assert.assertEquals(ns, q.getInvolvedNodes());
            Assert.assertFalse(q.isContinuous());
        });
    }
}
