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

package btrplace.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link DefaultShareableResource}.
 *
 * @author Fabien Hermenier
 */
public class DefaultShareableResourceTest {

    @Test
    public void testInstantiation() {
        ShareableResource rc = new DefaultShareableResource("foo");
        Assert.assertEquals("foo", rc.getIdentifier());
        Assert.assertEquals(DefaultShareableResource.DEFAULT_NO_VALUE, rc.getDefaultValue());

        rc = new DefaultShareableResource("bar", -7);
        Assert.assertEquals("bar", rc.getIdentifier());
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testDefinition() {
        ShareableResource rc = new DefaultShareableResource("foo");
        UUID id = UUID.randomUUID();
        Assert.assertFalse(rc.defined(id));
        Assert.assertEquals(rc.get(id), rc.getDefaultValue());

        rc.set(id, 3);
        Assert.assertTrue(rc.defined(id));
        Assert.assertEquals(3, rc.get(id));
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testGets() {
        ShareableResource rc = new DefaultShareableResource("foo");
        List<UUID> ids = new ArrayList<UUID>(10);
        for (int i = 0; i < 10; i++) {
            UUID id = UUID.randomUUID();
            ids.add(id);
            rc.set(id, i);
        }
        List<Integer> values = rc.get(ids);
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(values.get(i), (Integer) i);
        }
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testDefineds() {
        ShareableResource rc = new DefaultShareableResource("foo");
        List<UUID> ids = new ArrayList<UUID>(10);
        for (int i = 0; i < 10; i++) {
            UUID id = UUID.randomUUID();
            ids.add(id);
            rc.set(id, i);
        }
        Assert.assertTrue(rc.getDefined().containsAll(ids) && rc.getDefined().size() == ids.size());
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testUnset() {
        ShareableResource rc = new DefaultShareableResource("foo");
        UUID id = UUID.randomUUID();
        rc.set(id, 3);
        Assert.assertTrue(rc.unset(id));
        Assert.assertFalse(rc.defined(id));

        //Next, id is not defined so not 'unsetable'
        Assert.assertFalse(rc.unset(id));

    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testCompare() {
        ShareableResource rc = new DefaultShareableResource("foo");
        UUID i1 = UUID.randomUUID();
        rc.set(i1, 3);

        UUID i2 = UUID.randomUUID();
        rc.set(i2, 7);

        Assert.assertTrue(rc.compare(i1, i2) < 0);
        Assert.assertTrue(rc.compare(i2, i1) > 0);

        UUID i3 = UUID.randomUUID();
        rc.set(i3, 3);
        Assert.assertTrue(rc.compare(i3, i1) == 0);

        Assert.assertTrue(rc.compare(UUID.randomUUID(), i1) < 0);
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testMax() {
        ShareableResource rc = new DefaultShareableResource("foo");
        UUID i1 = UUID.randomUUID();
        rc.set(i1, 3);

        UUID i2 = UUID.randomUUID();
        rc.set(i2, 7);
        Assert.assertEquals(7, rc.max(rc.getDefined(), false));
        Set<UUID> x = new HashSet<UUID>();
        x.add(i1);
        Assert.assertEquals(3, rc.max(x, false));
        rc.set(i1, -15);
        x.add(UUID.randomUUID());
        Assert.assertEquals(-15, rc.max(x, false)); //If the default value would have been counted, it would have return 0
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testMin() {
        ShareableResource rc = new DefaultShareableResource("foo");
        UUID i1 = UUID.randomUUID();
        rc.set(i1, 3);

        UUID i2 = UUID.randomUUID();
        rc.set(i2, 7);
        Assert.assertEquals(3, rc.min(rc.getDefined(), false));
        Set<UUID> x = new HashSet<UUID>();
        x.add(i2);
        Assert.assertEquals(7, rc.min(x, false));
        rc.set(i2, 18);
        x.add(UUID.randomUUID());
        Assert.assertEquals(18, rc.min(x, false)); //If the default value would have been counted, it would have return 0
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testSum() {
        ShareableResource rc = new DefaultShareableResource("foo", -5); //-5 as default no code value to detect its presence in sum (would be an error)
        UUID i1 = UUID.randomUUID();
        rc.set(i1, 3);
        UUID i2 = UUID.randomUUID();
        rc.set(i2, 7);
        Assert.assertEquals(10, rc.sum(rc.getDefined(), false));
        Set<UUID> x = new HashSet<UUID>();
        x.add(i2);
        Assert.assertEquals(7, rc.sum(x, false));
        rc.set(i2, 18);
        x.clear();
        x.add(UUID.randomUUID());
        Assert.assertEquals(0, rc.sum(x, false));
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testToString() {
        ShareableResource rc = new DefaultShareableResource("foo");
        rc.set(UUID.randomUUID(), 1);
        rc.set(UUID.randomUUID(), 2);
        rc.set(UUID.randomUUID(), 3);
        //Simple test to be resilient
        Assert.assertNotNull(rc.toString());
    }


    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsAndHashCode() {
        ShareableResource rc1 = new DefaultShareableResource("foo");
        ShareableResource rc2 = new DefaultShareableResource("foo");
        ShareableResource rc3 = new DefaultShareableResource("bar");
        Assert.assertEquals(rc1, rc2);
        Assert.assertEquals(rc2, rc2);
        Assert.assertEquals(rc1.hashCode(), rc2.hashCode());
        Assert.assertNotEquals(rc1, rc3);
        Assert.assertNotEquals(rc3, rc2);
        Assert.assertNotEquals(rc1.hashCode(), rc3.hashCode());

        Assert.assertNotEquals(rc1, "foo");
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition", "testEqualsAndHashCode"})
    public void testClone() {
        ShareableResource rc1 = new DefaultShareableResource("foo", -1);
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();
        rc1.set(u1, 3);
        rc1.set(u2, 5);
        ShareableResource rc2 = rc1.clone();
        Assert.assertEquals(rc1, rc2);
        Assert.assertEquals(rc1.hashCode(), rc2.hashCode());

        rc1.set(u1, -5);
        Assert.assertNotEquals(rc1, rc2);

        rc1.set(u1, 3);
        Assert.assertEquals(rc1, rc2);

        rc2.unset(u2);
        Assert.assertNotEquals(rc1, rc2);

    }
}
