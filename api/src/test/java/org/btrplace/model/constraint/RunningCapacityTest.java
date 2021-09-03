/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link RunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class RunningCapacityTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        Set<Node> s = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        RunningCapacity c = new RunningCapacity(s, 3);
        Assert.assertNotNull(c.getChecker());
        Assert.assertEquals(s, c.getInvolvedNodes());
        Assert.assertEquals(3, c.getAmount());
        Assert.assertTrue(c.getInvolvedVMs().isEmpty());
        Assert.assertFalse(c.toString().contains("null"));

        Assert.assertFalse(c.isContinuous());
        Assert.assertFalse(c.setContinuous(true));
        System.out.println(c);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsAndHashCode() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);

        Set<Node> s = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));
        RunningCapacity c = new RunningCapacity(s, 3);
        RunningCapacity c2 = new RunningCapacity(s, 3);
        Assert.assertTrue(c.equals(c));
        Assert.assertTrue(c.equals(c2));
        Assert.assertEquals(c.hashCode(), c2.hashCode());

        Assert.assertFalse(c.equals(new RunningCapacity(s, 2)));
        Assert.assertFalse(c.equals(new RunningCapacity(new HashSet<>(), 3)));
    }

    @Test
    public void testDiscreteIsSatisfied() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);
        Mapping m = mo.getMapping();
        m.addOnlineNode(ns.get(0));
        m.addOnlineNode(ns.get(1));
        m.addRunningVM(vms.get(0), ns.get(0));
        m.addReadyVM(vms.get(1));
        m.addRunningVM(vms.get(2), ns.get(1));
        m.addReadyVM(vms.get(3));

        RunningCapacity c = new RunningCapacity(m.getAllNodes(), 2);
        c.setContinuous(false);
        Assert.assertEquals(c.isSatisfied(mo), true);
        m.addRunningVM(vms.get(1), ns.get(1));
        Assert.assertEquals(c.isSatisfied(mo), false);
    }
}
