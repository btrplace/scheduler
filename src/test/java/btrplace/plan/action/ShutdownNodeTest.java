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
 * Unit tests for {@link ShutdownNode}.
 *
 * @author Fabien Hermenier
 */
public class ShutdownNodeTest {

    @Test
    public void testInstantiate() {
        UUID n = UUID.randomUUID();
        ShutdownNode a = new ShutdownNode(n, 3, 5);
        Assert.assertEquals(n, a.getNode());
        Assert.assertEquals(3, a.getStart());
        Assert.assertEquals(5, a.getEnd());
        Assert.assertNotNull(a);
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testApply() {
        Mapping map = new DefaultMapping();
        Model m = new DefaultModel(map);
        UUID n = UUID.randomUUID();
        ShutdownNode a = new ShutdownNode(n, 3, 5);
        map.addOnlineNode(n);
        Assert.assertTrue(a.apply(m));
        Assert.assertTrue(map.getOfflineNodes().contains(n));

        Assert.assertFalse(a.apply(m));

        UUID vm = UUID.randomUUID();
        map.addOnlineNode(n);
        map.setVMRunOn(vm, n);
        Assert.assertFalse(a.apply(m));
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testEquals() {
        UUID n = UUID.randomUUID();
        ShutdownNode a = new ShutdownNode(n, 3, 5);
        ShutdownNode b = new ShutdownNode(n, 3, 5);
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertNotSame(a, new ShutdownNode(n, 4, 5));
        Assert.assertNotSame(a, new ShutdownNode(n, 3, 4));
        Assert.assertNotSame(a, new ShutdownNode(UUID.randomUUID(), 4, 5));
    }
}
