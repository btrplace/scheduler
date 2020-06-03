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
 * Unit tests for {@link Ready}.
 *
 * @author Fabien Hermenier
 */
public class ReadyTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Ready s = new Ready(v);
        Assert.assertNotNull(s.getChecker());
        Assert.assertEquals(Collections.singletonList(v), s.getInvolvedVMs());
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertNotNull(s.toString());
        Assert.assertTrue(s.setContinuous(false));
        System.out.println(s);
    }

    @Test
    public void testEquals() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Ready s = new Ready(v);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Ready(v).equals(s));
        Assert.assertEquals(new Ready(v).hashCode(), s.hashCode());
        Assert.assertFalse(new Ready(mo.newVM()).equals(s));
    }

    @Test
    public void testIsSatisfied() {
        Model i = new DefaultModel();
        Mapping c = i.getMapping();
        VM v = i.newVM();
        Node n = i.newNode();
        c.addOnlineNode(n);
        c.addReadyVM(v);
        Ready d = new Ready(v);
        Assert.assertEquals(d.isSatisfied(i), true);
        c.addRunningVM(v, n);
        Assert.assertEquals(d.isSatisfied(i), false);
        c.addSleepingVM(v, n);
        Assert.assertEquals(d.isSatisfied(i), false);
        c.remove(v);
        Assert.assertEquals(d.isSatisfied(i), false);
    }

    @Test
    public void testReady() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 5);
        List<Ready> c = Ready.newReady(vms);
        Assert.assertEquals(vms.size(), c.size());
        c.stream().forEach((q) -> {
            Assert.assertTrue(vms.containsAll(q.getInvolvedVMs()));
            Assert.assertFalse(q.isContinuous());
        });
    }
}
