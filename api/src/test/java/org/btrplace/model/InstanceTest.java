/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Online;
import org.btrplace.model.constraint.Running;
import org.btrplace.model.constraint.SatConstraint;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link org.btrplace.model.Instance}.
 *
 * @author Fabien Hermenier
 */
public class InstanceTest {

    @Test
    public void testInstantiation() {
        Model mo = Mockito.mock(Model.class);
        List<SatConstraint> l = new ArrayList<>();
        l.add(Mockito.mock(SatConstraint.class));
        MinMTTR o = new MinMTTR();
        Instance i = new Instance(mo, l, o);
        Assert.assertEquals(i.getModel(), mo);
        Assert.assertEquals(i.getSatConstraints(), l);
        Assert.assertEquals(i.getOptConstraint(), o);
        i = new Instance(mo, o);
        Assert.assertEquals(i.getSatConstraints().size(), 0);
    }

    @Test
    public void testEqualsAndHashcode() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);

        Mapping ma = mo.getMapping();
        ma.addOnlineNode(ns.get(0));
        ma.addOfflineNode(ns.get(0));
        ma.addReadyVM(vms.get(0));
        List<SatConstraint> cstrs = new ArrayList<>(Online.newOnline(ma.getAllNodes()));
        cstrs.add(new Running(vms.get(0)));
        Instance i = new Instance(mo, cstrs, new MinMTTR());
        Instance i2 = new Instance(mo.copy(), new ArrayList<>(cstrs), new MinMTTR());

        Assert.assertEquals(i, i2);
        Assert.assertEquals(i, i);
        Assert.assertNotEquals(i, null);
        Assert.assertNotEquals(ma, i);
        Assert.assertEquals(i.hashCode(), i2.hashCode());

        i2.getModel().getMapping().addReadyVM(vms.get(2));
        Assert.assertNotEquals(i, i2);

    }
}
