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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link btrplace.model.constraint.Online}.
 *
 * @author Fabien Hermenier
 */
public class OnlineTest extends ConstraintTestMaterial {

    @Test
    public void testInstantiation() {
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2));
        Online o = new Online(s);
        Assert.assertEquals(o.getInvolvedNodes(), s);
        Assert.assertTrue(o.getInvolvedVMs().isEmpty());
        Assert.assertNotNull(o.toString());
        System.out.println(o);
    }

    @Test
    public void testIsSatisfied() {
        Mapping c = new DefaultMapping();
        c.addOnlineNode(n1);
        c.addOnlineNode(n2);
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2));
        Online o = new Online(s);

        Model i = new DefaultModel(c);

        Assert.assertEquals(o.isSatisfied(i), SatConstraint.Sat.SATISFIED);
        c.addOfflineNode(n2);
        Assert.assertEquals(o.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);
    }

    @Test
    public void testEquals() {
        Set<UUID> x = new HashSet<UUID>(Arrays.asList(n1, n2));
        Online s = new Online(x);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Online(x).equals(s));
        Assert.assertEquals(new Online(x).hashCode(), s.hashCode());
        x = new HashSet<UUID>(Arrays.asList(n3));
        Assert.assertFalse(new Online(x).equals(s));
    }
}
