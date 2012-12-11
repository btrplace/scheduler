/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace.plan.event;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link KillVM}.
 *
 * @author Fabien Hermenier
 */
public class KillVMTest {

    @Test
    public void testInstantiate() {
        UUID vm = UUID.randomUUID();
        UUID n = UUID.randomUUID();
        KillVM a = new KillVM(vm, n, 3, 5);
        Assert.assertEquals(vm, a.getVM());
        Assert.assertEquals(n, a.getNode());
        Assert.assertEquals(3, a.getStart());
        Assert.assertEquals(5, a.getEnd());
        Assert.assertFalse(a.toString().contains("null"));

    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testApply() {
        Mapping map = new DefaultMapping();
        Model m = new DefaultModel(map);
        UUID vm = UUID.randomUUID();
        UUID n = UUID.randomUUID();
        KillVM a = new KillVM(vm, n, 3, 5);
        map.addOnlineNode(n);
        map.addRunningVM(vm, n);
        Assert.assertTrue(a.apply(m));
        Assert.assertFalse(map.containsVM(vm));

        Assert.assertFalse(a.apply(m));

        map.addSleepingVM(vm, n);
        Assert.assertTrue(a.apply(m));

        map.addReadyVM(vm);
        Assert.assertTrue(a.apply(m));
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testEquals() {
        UUID n = UUID.randomUUID();
        UUID vm = UUID.randomUUID();
        KillVM a = new KillVM(vm, n, 3, 5);
        KillVM b = new KillVM(vm, n, 3, 5);
        Assert.assertFalse(a.equals(new Object()));
        Assert.assertTrue(a.equals(a));
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertNotSame(a, new KillVM(vm, n, 4, 5));
        Assert.assertNotSame(a, new KillVM(vm, n, 3, 4));
        Assert.assertNotSame(a, new KillVM(vm, UUID.randomUUID(), 3, 5));
        Assert.assertNotSame(a, new KillVM(UUID.randomUUID(), n, 4, 5));
    }
}
