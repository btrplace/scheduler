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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link SingleResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class SingleResourceCapacityTest {

    @Test
    public void testInstantiation() {
        Set<UUID> s = new HashSet<UUID>();
        s.add(UUID.randomUUID());
        s.add(UUID.randomUUID());
        SingleResourceCapacity c = new SingleResourceCapacity(s, "foo", 3);
        Assert.assertEquals(s, c.getInvolvedNodes());
        Assert.assertEquals("foo", c.getResource());
        Assert.assertEquals(3, c.getAmount());
        Assert.assertTrue(c.getInvolvedVMs().isEmpty());
        Assert.assertFalse(c.toString().contains("null"));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsAndHashCode() {
        Set<UUID> s = new HashSet<UUID>();
        s.add(UUID.randomUUID());
        s.add(UUID.randomUUID());
        SingleResourceCapacity c = new SingleResourceCapacity(s, "foo", 3);
        SingleResourceCapacity c2 = new SingleResourceCapacity(s, "foo", 3);
        Assert.assertTrue(c.equals(c));
        Assert.assertTrue(c.equals(c2));
        Assert.assertEquals(c.hashCode(), c2.hashCode());

        Assert.assertFalse(c.equals(new SingleResourceCapacity(s, "bar", 3)));
        Assert.assertFalse(c.equals(new SingleResourceCapacity(s, "foo", 2)));
        Assert.assertFalse(c.equals(new SingleResourceCapacity(new HashSet<UUID>(), "foo", 3)));
    }
}
