/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.model;

import btrplace.model.constraint.Online;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.test.PremadeElements;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link btrplace.model.Instance}.
 *
 * @author Fabien Hermenier
 */
public class InstanceTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Model mo = Mockito.mock(Model.class);
        List<SatConstraint> l = new ArrayList<>();
        l.add(Mockito.mock(SatConstraint.class));
        Instance i = new Instance(mo, l);
        Assert.assertEquals(i.getModel(), mo);
        Assert.assertEquals(i.getConstraints(), l);
    }

    @Test
    public void testEqualsAndHashcode() {
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();
        ma.addOnlineNode(n1);
        ma.addOfflineNode(n1);
        ma.addReadyVM(vm1);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Online(ma.getAllNodes()));
        cstrs.add(new Running(Collections.singleton(vm1)));
        Instance i = new Instance(mo, cstrs);
        Instance i2 = new Instance(mo.clone(), new ArrayList<>(cstrs));

        Assert.assertEquals(i, i2);
        Assert.assertEquals(i.hashCode(), i2.hashCode());

        i2.getModel().getMapping().addReadyVM(vm3);
        Assert.assertNotEquals(i, i2);

    }
}
