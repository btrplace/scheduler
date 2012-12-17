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
 * Unit tests for {@link Overbook}.
 *
 * @author Fabien Hermenier
 */
public class OverbookTest {

    @Test
    public void testInstantiation() {
        Set<UUID> s = new HashSet<UUID>();
        s.add(UUID.randomUUID());
        s.add(UUID.randomUUID());
        Overbook o = new Overbook(s, "foo", 1.5);
        Assert.assertEquals(s, o.getInvolvedNodes());
        Assert.assertEquals("foo", o.getResource());
        Assert.assertTrue(o.getInvolvedVMs().isEmpty());
        Assert.assertEquals(1.5, o.getRatio());
        Assert.assertNotNull(o.toString());
    }

    @Test
    public void testIsSatisfied() {
        Set<UUID> s = new HashSet<UUID>();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();

        s.add(n1);
        s.add(n2);

        Mapping cfg = new DefaultMapping();
        cfg.addOnlineNode(n1);
        cfg.addOnlineNode(n2);

        ShareableResource rc = new DefaultShareableResource("cpu");
        rc.set(n1, 1);
        rc.set(n2, 4);

        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        UUID vm4 = UUID.randomUUID();

        rc.set(vm1, 2);
        rc.set(vm2, 2);
        rc.set(vm3, 4);

        cfg.addRunningVM(vm1, n1);
        cfg.addRunningVM(vm2, n2);
        cfg.addRunningVM(vm3, n2);
        cfg.addRunningVM(vm4, n2);


        Model i = new DefaultModel(cfg);
        i.attach(rc);

        Overbook o = new Overbook(s, "cpu", 2);
        Assert.assertEquals(o.isSatisfied(i), SatConstraint.Sat.SATISFIED);

        rc.set(vm1, 4);
        Assert.assertEquals(o.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);

        cfg.addRunningVM(vm1, n2);
        Assert.assertEquals(o.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);

        Overbook o2 = new Overbook(s, "mem", 2);
        Assert.assertEquals(o2.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);
    }

    @Test
    public void testEquals() {
        Set<UUID> x = new HashSet<UUID>();
        x.add(UUID.randomUUID());
        x.add(UUID.randomUUID());
        Overbook s = new Overbook(x, "foo", 3);

        Assert.assertTrue(s.equals(s));
        Overbook o2 = new Overbook(x, "foo", 3);
        Assert.assertTrue(o2.equals(s));
        Assert.assertEquals(o2.hashCode(), s.hashCode());
        Assert.assertFalse(new Overbook(x, "bar", 3).equals(s));
        Assert.assertFalse(new Overbook(x, "foo", 2).equals(s));
        x = new HashSet<UUID>();
        x.add(UUID.randomUUID());
        Assert.assertFalse(new Overbook(x, "foo", 3).equals(s));
    }
}
