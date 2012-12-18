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
 * Unit tests for {@link Lonely}.
 *
 * @author Fabien Hermenier
 */
public class LonelyTest {

    @Test
    public void testInstantiation() {
        Set<UUID> s = new HashSet<UUID>();
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        s.add(vm1);
        s.add(vm2);
        s.add(vm3);
        Lonely l = new Lonely(s);
        Assert.assertFalse(l.toString().contains("null"));
        Assert.assertEquals(l.getInvolvedVMs(), s);
        Assert.assertTrue(l.getInvolvedNodes().isEmpty());
        Assert.assertFalse(l.isContinuous());
        Assert.assertFalse(l.setContinuous(true));
        Assert.assertTrue(l.setContinuous(false));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsHashCode() {
        Set<UUID> s = new HashSet<UUID>();
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        s.add(vm1);
        s.add(vm2);
        s.add(vm3);
        Lonely l = new Lonely(s);
        Assert.assertTrue(l.equals(l));
        Assert.assertTrue(l.equals(new Lonely(new HashSet<UUID>(s))));
        Assert.assertEquals(l.hashCode(), new Lonely(new HashSet<UUID>(s)).hashCode());
        Assert.assertFalse(l.equals(new Lonely(new HashSet<UUID>())));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testDiscreteIsSatisfied() {
        Set<UUID> s = new HashSet<UUID>();
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        UUID vm4 = UUID.randomUUID();

        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID n3 = UUID.randomUUID();

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addSleepingVM(vm4, n2);

        Model mo = new DefaultModel(map);

        s.add(vm1);
        s.add(vm2);
        Lonely l = new Lonely(s);

        Assert.assertEquals(l.isSatisfied(mo), SatConstraint.Sat.SATISFIED);

        s.add(vm4);
        Assert.assertEquals(l.isSatisfied(mo), SatConstraint.Sat.SATISFIED);

        map.addRunningVM(vm3, n1);
        Assert.assertEquals(l.isSatisfied(mo), SatConstraint.Sat.UNSATISFIED);
    }
}
