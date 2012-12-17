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
 * Unit tests for {@link btrplace.model.constraint.Online}.
 *
 * @author Fabien Hermenier
 */
public class OnlineTest {

    @Test
    public void testInstantiation() {
        Set<UUID> s = new HashSet<UUID>();
        s.add(UUID.randomUUID());
        s.add(UUID.randomUUID());
        Online o = new Online(s);
        Assert.assertEquals(o.getInvolvedNodes(), s);
        Assert.assertTrue(o.getInvolvedVMs().isEmpty());
        Assert.assertNotNull(o.toString());
    }

    @Test
    public void testIsSatisfied() {
        Mapping c = new DefaultMapping();
        UUID n = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        c.addOnlineNode(n);
        c.addOnlineNode(n2);
        Set<UUID> s = new HashSet<UUID>();
        s.add(n);
        s.add(n2);
        Online o = new Online(s);

        Model i = new DefaultModel(c);

        Assert.assertEquals(o.isSatisfied(i), SatConstraint.Sat.SATISFIED);
        c.addOfflineNode(n2);
        Assert.assertEquals(o.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);
    }

    @Test
    public void testEquals() {
        Set<UUID> x = new HashSet<UUID>();
        x.add(UUID.randomUUID());
        x.add(UUID.randomUUID());
        Online s = new Online(x);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Online(x).equals(s));
        Assert.assertEquals(new Online(x).hashCode(), s.hashCode());
        x = new HashSet<UUID>();
        x.add(UUID.randomUUID());
        Assert.assertFalse(new Online(x).equals(s));
    }
}
