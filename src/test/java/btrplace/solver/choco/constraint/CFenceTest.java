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

package btrplace.solver.choco.constraint;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Fence;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link btrplace.model.constraint.Fence}.
 *
 * @author Fabien Hermenier
 */
public class CFenceTest {

    /**
     * Test getMisPlaced() in various situations.
     */
    @Test
    public void testGetMisPlaced() {
        Mapping m = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID n3 = UUID.randomUUID();
        UUID n4 = UUID.randomUUID();
        UUID n5 = UUID.randomUUID();

        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        UUID vm4 = UUID.randomUUID();
        UUID vm5 = UUID.randomUUID();

        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);
        m.addOnlineNode(n4);
        m.addOfflineNode(n5);

        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm2, n1);
        m.addRunningVM(vm3, n2);
        m.addRunningVM(vm4, n3);
        m.addSleepingVM(vm5, n4);

        Set<UUID> vms = new HashSet<UUID>();
        Set<UUID> ns = new HashSet<UUID>();

        vms.add(vm1);
        vms.add(vm2);
        ns.add(n1);
        ns.add(n2);
        CFence c = new CFence(new Fence(vms, ns));
        Model mo = new DefaultModel(m);
        Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        ns.add(vm5);
        Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        vms.add(vm3);
        Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        vms.add(vm4);
        Set<UUID> bad = c.getMisPlacedVMs(mo);
        Assert.assertEquals(1, bad.size());
        Assert.assertTrue(bad.contains(vm4));
    }
}
