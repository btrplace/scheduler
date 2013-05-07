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

package btrplace.model.view;

import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link ShareableResource}.
 *
 * @author Fabien Hermenier
 */
public class ShareableResourceTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        ShareableResource rc = new ShareableResource("foo");
        Assert.assertEquals(rc.getIdentifier(), "ShareableResource.foo");
        Assert.assertEquals(rc.getResourceIdentifier(), "foo");
        Assert.assertEquals(rc.getDefaultValue(), ShareableResource.DEFAULT_NO_VALUE);

        rc = new ShareableResource("bar", -7);
        Assert.assertEquals(rc.getIdentifier(), "ShareableResource.bar");
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testDefinition() {
        ShareableResource rc = new ShareableResource("foo");
        Assert.assertFalse(rc.defined(vm1));
        Assert.assertEquals(rc.get(vm1), rc.getDefaultValue());

        rc.set(vm1, 3);
        Assert.assertTrue(rc.defined(vm1));
        Assert.assertEquals(rc.get(vm1), 3);
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testGets() {
        ShareableResource rc = new ShareableResource("foo");
        List<UUID> ids = new ArrayList<>(10);
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
    public void testDefined() {
        ShareableResource rc = new ShareableResource("foo");
        List<UUID> ids = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            UUID id = UUID.randomUUID();
            ids.add(id);
            rc.set(id, i);
        }
        Assert.assertTrue(rc.getDefined().containsAll(ids) && rc.getDefined().size() == ids.size());
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testUnset() {
        ShareableResource rc = new ShareableResource("foo");
        rc.set(vm1, 3);
        Assert.assertTrue(rc.unset(vm1));
        Assert.assertFalse(rc.defined(vm1));

        //Next, id is not defined so not 'unsetable'
        Assert.assertFalse(rc.unset(vm1));

    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testCompare() {
        ShareableResource rc = new ShareableResource("foo");
        rc.set(vm1, 3);

        rc.set(vm2, 7);

        Assert.assertTrue(rc.compare(vm1, vm2) < 0);
        Assert.assertTrue(rc.compare(vm2, vm1) > 0);

        rc.set(vm3, 3);
        Assert.assertTrue(rc.compare(vm3, vm1) == 0);

        Assert.assertTrue(rc.compare(vm4, vm1) < 0);
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testMax() {
        ShareableResource rc = new ShareableResource("foo");
        rc.set(vm1, 3);

        rc.set(vm2, 7);
        Assert.assertEquals(7, rc.max(rc.getDefined(), false));
        Set<UUID> x = new HashSet<>();
        x.add(vm1);
        Assert.assertEquals(3, rc.max(x, false));
        rc.set(vm1, -15);
        x.add(vm3);
        Assert.assertEquals(-15, rc.max(x, false)); //If the default value would have been counted, it would have return 0
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testMin() {
        ShareableResource rc = new ShareableResource("foo");
        rc.set(vm1, 3);

        rc.set(vm2, 7);
        Assert.assertEquals(3, rc.min(rc.getDefined(), false));
        Set<UUID> x = new HashSet<>();
        x.add(vm2);
        Assert.assertEquals(7, rc.min(x, false));
        rc.set(vm2, 18);
        x.add(vm3);
        Assert.assertEquals(18, rc.min(x, false)); //If the default value would have been counted, it would have return 0
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testSum() {
        ShareableResource rc = new ShareableResource("foo", -5); //-5 as default no code value to detect its presence in sum (would be an error)

        rc.set(vm1, 3);
        rc.set(vm2, 7);
        Assert.assertEquals(10, rc.sum(rc.getDefined(), false));
        Set<UUID> x = new HashSet<>();
        x.add(vm2);
        Assert.assertEquals(7, rc.sum(x, false));
        rc.set(vm2, 18);
        x.clear();
        x.add(vm3);
        Assert.assertEquals(0, rc.sum(x, false));
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testToString() {
        ShareableResource rc = new ShareableResource("foo");
        rc.set(vm1, 1);
        rc.set(vm2, 2);
        rc.set(vm3, 3);
        //Simple test to be resilient
        Assert.assertNotNull(rc.toString());
    }


    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsAndHashCode() {
        ShareableResource rc1 = new ShareableResource("foo");
        ShareableResource rc2 = new ShareableResource("foo");
        ShareableResource rc3 = new ShareableResource("bar");
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
        ShareableResource rc1 = new ShareableResource("foo", -1);
        rc1.set(vm1, 3);
        rc1.set(vm2, 5);
        ShareableResource rc2 = rc1.clone();
        Assert.assertEquals(rc1, rc2);
        Assert.assertEquals(rc1.hashCode(), rc2.hashCode());

        rc1.set(vm1, -5);
        Assert.assertNotEquals(rc1, rc2);

        rc1.set(vm1, 3);
        Assert.assertEquals(rc1, rc2);

        rc2.unset(vm2);
        Assert.assertNotEquals(rc1, rc2);
    }

    @Test
    public void testSubstitution() {
        ShareableResource rc = new ShareableResource("foo");
        rc.set(vm1, 3);
        Assert.assertTrue(rc.substitute(vm1, vm10));
        Assert.assertEquals(rc.get(vm10), 3);
        Assert.assertTrue(rc.substitute(vm3, vm7));
        Assert.assertEquals(rc.get(vm7), 0);
    }
}
