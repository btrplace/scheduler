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
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link CumulatedRunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CumulatedRunningCapacityTest {

    @Test
    public void testInstantiation() {
        Set<UUID> s = new HashSet<UUID>();
        s.add(UUID.randomUUID());
        s.add(UUID.randomUUID());
        CumulatedRunningCapacity c = new CumulatedRunningCapacity(s, 3);
        Assert.assertEquals(s, c.getInvolvedNodes());
        Assert.assertEquals(3, c.getAmount());
        Assert.assertTrue(c.getInvolvedVMs().isEmpty());
        Assert.assertFalse(c.toString().contains("null"));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsAndHashCode() {
        Set<UUID> s = new HashSet<UUID>();
        s.add(UUID.randomUUID());
        s.add(UUID.randomUUID());
        CumulatedRunningCapacity c = new CumulatedRunningCapacity(s, 3);
        CumulatedRunningCapacity c2 = new CumulatedRunningCapacity(s, 3);
        Assert.assertTrue(c.equals(c));
        Assert.assertTrue(c.equals(c2));
        Assert.assertEquals(c.hashCode(), c2.hashCode());

        Assert.assertFalse(c.equals(new CumulatedRunningCapacity(s, 2)));
        Assert.assertFalse(c.equals(new CumulatedRunningCapacity(new HashSet<UUID>(), 3)));
    }

    @Test
    public void testIsSatisfied() {
        Mapping m = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        UUID vm4 = UUID.randomUUID();
        m.addRunningVM(vm1, n1);
        m.addReadyVM(vm2);

        m.addRunningVM(vm3, n2);
        m.addReadyVM(vm4);
        Model mo = new DefaultModel(m);
        CumulatedRunningCapacity c = new CumulatedRunningCapacity(m.getAllNodes(), 2);
        Assert.assertEquals(c.isSatisfied(mo), SatConstraint.Sat.SATISFIED);
        m.addRunningVM(vm2, n2);
        Assert.assertEquals(c.isSatisfied(mo), SatConstraint.Sat.UNSATISFIED);
    }
}
