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

package btrplace.model.constraint;

import btrplace.model.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link Ban}.
 *
 * @author Fabien Hermenier
 */
public class BanTest {

    @Test
    public void testInstantiation() {
        Set<UUID> vms = new HashSet<UUID>();
        Set<UUID> nodes = new HashSet<UUID>();
        vms.add(UUID.randomUUID());
        nodes.add(UUID.randomUUID());
        Ban b = new Ban(vms, nodes);
        Assert.assertEquals(vms, b.getInvolvedVMs());
        Assert.assertEquals(nodes, b.getInvolvedNodes());
        Assert.assertFalse(b.toString().contains("null"));
        Assert.assertFalse(b.isContinuous());
        Assert.assertFalse(b.setContinuous(true));
        System.out.println(b);
    }

    @Test
    public void testIsSatisfied() {

        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID n3 = UUID.randomUUID();
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n3);
        Set<UUID> vms = new HashSet<UUID>();
        vms.add(vm2);
        vms.add(vm3);

        Set<UUID> nodes = new HashSet<UUID>();
        nodes.add(n1);
        Ban b = new Ban(vms, nodes);
        Model m = new DefaultModel(map);
        Assert.assertEquals(b.isSatisfied(m), SatConstraint.Sat.SATISFIED);
        map.addRunningVM(vm3, n1);
        Assert.assertEquals(b.isSatisfied(m), SatConstraint.Sat.UNSATISFIED);
    }

    @Test
    public void testEquals() {
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        Set<UUID> vms = new HashSet<UUID>();
        vms.add(vm1);
        vms.add(vm2);
        Set<UUID> nodes = new HashSet<UUID>();
        nodes.add(n1);
        nodes.add(n2);
        Ban b = new Ban(vms, nodes);
        Assert.assertTrue(b.equals(b));
        Assert.assertTrue(new Ban(vms, nodes).equals(b));
        Assert.assertEquals(new Ban(vms, nodes).hashCode(), b.hashCode());

        Assert.assertFalse(new Ban(nodes, vms).equals(b));
    }
}
