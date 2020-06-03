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
 * Unit tests for {@link org.btrplace.model.constraint.Sleeping}.
 *
 * @author Fabien Hermenier
 */
public class SleepingTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();

        Sleeping s = new Sleeping(v);
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
        Sleeping s = new Sleeping(v);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Sleeping(v).equals(s));
        Assert.assertEquals(new Sleeping(v).hashCode(), s.hashCode());
        Assert.assertFalse(new Sleeping(mo.newVM()).equals(s));
    }

    @Test
    public void testIsSatisfied() {
        Model i = new DefaultModel();
        VM v = i.newVM();
        Node n = i.newNode();

        Mapping c = i.getMapping();

        c.addOnlineNode(n);
        c.addSleepingVM(v, n);
        Sleeping d = new Sleeping(v);
        Assert.assertEquals(d.isSatisfied(i), true);
        c.addReadyVM(v);
        Assert.assertEquals(d.isSatisfied(i), false);
        c.addRunningVM(v, n);
        Assert.assertEquals(d.isSatisfied(i), false);
        c.remove(v);
        Assert.assertEquals(d.isSatisfied(i), false);
    }

    @Test
    public void testSleeping() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 5);
        List<Sleeping> c = Sleeping.newSleeping(vms);
        Assert.assertEquals(vms.size(), c.size());
        c.stream().forEach((q) -> {
            Assert.assertTrue(vms.containsAll(q.getInvolvedVMs()));
            Assert.assertFalse(q.isContinuous());
        });
    }
}
