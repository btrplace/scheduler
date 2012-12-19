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
 * Unit tests for {@link btrplace.model.constraint.Sleeping}.
 *
 * @author Fabien Hermenier
 */
public class SleepingTest {

    @Test
    public void testInstantiation() {
        Set<UUID> x = new HashSet<UUID>();
        x.add(UUID.randomUUID());
        x.add(UUID.randomUUID());
        Sleeping s = new Sleeping(x);
        Assert.assertEquals(x, s.getInvolvedVMs());
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertNotNull(s.toString());
        System.out.println(s);
    }

    @Test
    public void testEquals() {
        Set<UUID> x = new HashSet<UUID>();
        x.add(UUID.randomUUID());
        x.add(UUID.randomUUID());
        Sleeping s = new Sleeping(x);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Sleeping(x).equals(s));
        Assert.assertEquals(new Sleeping(x).hashCode(), s.hashCode());
        x = new HashSet<UUID>();
        x.add(UUID.randomUUID());
        Assert.assertFalse(new Sleeping(x).equals(s));
    }

    @Test
    public void testIsSatisfied() {
        Mapping c = new DefaultMapping();
        UUID v1 = UUID.randomUUID();
        UUID v2 = UUID.randomUUID();
        Set<UUID> s = new HashSet<UUID>();
        s.add(v1);
        s.add(v2);
        UUID n = UUID.randomUUID();
        c.addOnlineNode(n);
        c.addSleepingVM(v1, n);
        c.addSleepingVM(v2, n);
        Sleeping d = new Sleeping(s);
        Model i = new DefaultModel(c);
        Assert.assertEquals(d.isSatisfied(i), SatConstraint.Sat.SATISFIED);
        c.addReadyVM(v1);
        Assert.assertEquals(d.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);
        c.addRunningVM(v1, n);
        Assert.assertEquals(d.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);
        c.removeVM(v1);
        Assert.assertEquals(d.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);
    }
}
