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

package btrplace.plan.action;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link MigrateVM}.
 *
 * @author Fabien Hermenier
 */
public class MigrateVMTest {

    @Test
    public void testInstantiate() {
        UUID vm = UUID.randomUUID();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();

        MigrateVM a = new MigrateVM(vm, n1, n2, 3, 5);
        Assert.assertEquals(vm, a.getVM());
        Assert.assertEquals(n1, a.getSourceNode());
        Assert.assertEquals(n2, a.getDestinationNode());
        Assert.assertEquals(3, a.getStart());
        Assert.assertEquals(5, a.getEnd());
        Assert.assertFalse(a.toString().contains("null"));

    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testApply() {
        Mapping map = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm, n1);

        Model m = new DefaultModel(map);

        MigrateVM a = new MigrateVM(vm, n1, n2, 3, 5);
        Assert.assertTrue(a.apply(m));
        Assert.assertEquals(map.getVMLocation(vm), n2);
        Assert.assertTrue(map.getRunningVMs().contains(vm));

        Assert.assertFalse(a.apply(m));
        Assert.assertEquals(map.getVMLocation(vm), n2);

        Assert.assertFalse(new MigrateVM(vm, n2, n2, 3, 5).apply(m));

        map.addSleepingVM(vm, n2);
        Assert.assertFalse(new MigrateVM(vm, n2, n1, 3, 5).apply(m));

        map.addReadyVM(vm);
        Assert.assertFalse(new MigrateVM(vm, n2, n1, 3, 5).apply(m));

        map.addOfflineNode(n1);
        Assert.assertFalse(new MigrateVM(vm, n2, n1, 3, 5).apply(m));

        map.removeNode(n1);
        Assert.assertFalse(new MigrateVM(vm, n2, n1, 3, 5).apply(m));
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testEquals() {
        UUID vm = UUID.randomUUID();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();

        MigrateVM a = new MigrateVM(vm, n1, n2, 3, 5);
        MigrateVM b = new MigrateVM(vm, n1, n2, 3, 5);
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());

        Assert.assertNotSame(a, new MigrateVM(vm, n1, n2, 4, 5));
        Assert.assertNotSame(a, new MigrateVM(vm, n1, n2, 3, 4));
        Assert.assertNotSame(a, new MigrateVM(UUID.randomUUID(), n1, n2, 3, 5));
        Assert.assertNotSame(a, new MigrateVM(vm, UUID.randomUUID(), n2, 3, 5));
        Assert.assertNotSame(a, new MigrateVM(vm, n1, UUID.randomUUID(), 3, 5));

    }
}
