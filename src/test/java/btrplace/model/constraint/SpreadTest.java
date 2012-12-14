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
 * Unit tests for {@link btrplace.model.constraint.Spread}.
 *
 * @author Fabien Hermenier
 */
public class SpreadTest {

    @Test
    public void testInstantiation() {
        Set<UUID> x = new HashSet<UUID>();
        x.add(UUID.randomUUID());
        x.add(UUID.randomUUID());
        Spread s = new Spread(x);
        Assert.assertEquals(x, s.getInvolvedVMs());
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertNotNull(s.toString());
    }

    @Test
    public void testEquals() {
        Set<UUID> x = new HashSet<UUID>();
        x.add(UUID.randomUUID());
        x.add(UUID.randomUUID());
        Spread s = new Spread(x);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Spread(x).equals(s));
        Assert.assertEquals(s.hashCode(), new Spread(x).hashCode());
        x = new HashSet<UUID>();
        x.add(UUID.randomUUID());
        Assert.assertFalse(new Spread(x).equals(s));
        Assert.assertNotSame(s.hashCode(), new Spread(x).hashCode());
    }

    @Test
    public void testIsSatisfied() {
        Mapping c = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID n3 = UUID.randomUUID();
        c.addOnlineNode(n1);
        c.addOnlineNode(n2);
        c.addOnlineNode(n3);
        Set<UUID> s = new HashSet<UUID>();
        UUID v1 = UUID.randomUUID();
        UUID v2 = UUID.randomUUID();
        UUID v3 = UUID.randomUUID();
        s.add(v1);
        s.add(v2);
        s.add(v3);
        Spread sp = new Spread(s);

        Model i = new DefaultModel(c);
        Assert.assertEquals(sp.isSatisfied(i), SatConstraint.Sat.SATISFIED);
        c.addRunningVM(v1, n2);
        c.addReadyVM(v2);
        c.addRunningVM(v2, n1);
        Assert.assertEquals(sp.isSatisfied(i), SatConstraint.Sat.SATISFIED);
        c.addSleepingVM(v3, n1);
        Assert.assertEquals(sp.isSatisfied(i), SatConstraint.Sat.SATISFIED);
        c.addRunningVM(v3, n1);
        Assert.assertEquals(sp.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);

    }
}
