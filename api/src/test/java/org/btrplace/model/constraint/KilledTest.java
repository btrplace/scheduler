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
 * Unit tests for {@link Killed}.
 *
 * @author Fabien Hermenier
 */
public class KilledTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();

        VM v = mo.newVM();
        Killed s = new Killed(v);
        Assert.assertNotNull(s.getChecker());
        Assert.assertEquals(Collections.singletonList(v), s.getInvolvedVMs());
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertNotNull(s.toString());
        System.out.println(s);
        Assert.assertFalse(s.setContinuous(true));
    }

    @Test
    public void testEquals() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Killed s = new Killed(v);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Killed(v).equals(s));
        Assert.assertEquals(new Killed(v).hashCode(), s.hashCode());
        Assert.assertFalse(new Killed(mo.newVM()).equals(s));
        Assert.assertFalse(new Killed(mo.newVM()).equals(new Object()));
    }

    @Test
    public void testIsSatisfied() {
        Model i = new DefaultModel();
        VM v = i.newVM();
        Node n = i.newNode();

        Mapping c = i.getMapping();
        Killed d = new Killed(v);
        Assert.assertEquals(d.isSatisfied(i), true);
        c.addReadyVM(v);
        Assert.assertEquals(d.isSatisfied(i), false);
        c.addOnlineNode(n);
        c.addRunningVM(v, n);
        Assert.assertEquals(d.isSatisfied(i), false);
        c.addSleepingVM(v, n);
        Assert.assertEquals(d.isSatisfied(i), false);
    }

    @Test
    public void testKilled() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 5);
        List<Killed> c = Killed.newKilled(vms);
        Assert.assertEquals(vms.size(), c.size());
        c.stream().forEach((q) -> {
            Assert.assertTrue(vms.containsAll(q.getInvolvedVMs()));
            Assert.assertFalse(q.isContinuous());
        });
    }
}
