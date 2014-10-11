/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Online.newOnline(ma.getAllNodes()));
        cstrs.add(new Running(vms.get(0)));
        Instance i = new Instance(mo, cstrs, new MinMTTR());
        Instance i2 = new Instance(mo.clone(), new ArrayList<>(cstrs), new MinMTTR());

        Assert.assertEquals(i, i2);
        Assert.assertEquals(i.hashCode(), i2.hashCode());

        i2.getModel().getMapping().addReadyVM(vms.get(2));
        Assert.assertNotEquals(i, i2);

    }
}
