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

import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link org.btrplace.model.constraint.Running}.
 *
 * @author Fabien Hermenier
 */
public class RunningTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        VM vm = mo.newVM();
        Running s = new Running(vm);
        Assert.assertNotNull(s.getChecker());
        Assert.assertEquals(Collections.singletonList(vm), s.getInvolvedVMs());
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertNotNull(s.toString());
        Assert.assertTrue(s.setContinuous(false));
        System.out.println(s);
    }

    @Test
    public void testEquals() {
        Model mo = new DefaultModel();
        VM vm = mo.newVM();
        Running s = new Running(vm);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Running(vm).equals(s));
        Assert.assertEquals(new Running(vm).hashCode(), s.hashCode());
        Assert.assertFalse(new Running(mo.newVM()).equals(s));
    }

    @Test
    public void testIsSatisfied() {
        Model i = new DefaultModel();
        VM vm = i.newVM();
        List<Node> ns = Util.newNodes(i, 2);
        Mapping c = i.getMapping();
        c.addOnlineNode(ns.get(0));
        c.addRunningVM(vm, ns.get(0));
        Running d = new Running(vm);
        Assert.assertEquals(d.isSatisfied(i), true);
        c.addReadyVM(vm);
        Assert.assertEquals(d.isSatisfied(i), false);
        c.addSleepingVM(vm, ns.get(0));
        Assert.assertEquals(d.isSatisfied(i), false);
        c.remove(vm);
        Assert.assertEquals(d.isSatisfied(i), false);
    }

    @Test
    public void testRunnings() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 5);
        List<Running> c = Running.newRunning(vms);
        Assert.assertEquals(vms.size(), c.size());
        c.stream().forEach((q) -> {
            Assert.assertTrue(vms.containsAll(q.getInvolvedVMs()));
            Assert.assertFalse(q.isContinuous());
        });
    }
}
