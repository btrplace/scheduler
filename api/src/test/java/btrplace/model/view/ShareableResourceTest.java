/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

    private static Random rnd = new Random();

    @Test
    public void testInstantiation() {
        ShareableResource rc = new ShareableResource("foo");
        Assert.assertEquals(rc.getIdentifier(), "ShareableResource.foo");
        Assert.assertEquals(rc.getResourceIdentifier(), "foo");
        Assert.assertEquals(rc.getDefaultCapacity(), ShareableResource.DEFAULT_NO_VALUE);
        Assert.assertEquals(rc.getDefaultConsumption(), ShareableResource.DEFAULT_NO_VALUE);

        rc = new ShareableResource("bar", -7, 3);
        Assert.assertEquals(rc.getIdentifier(), "ShareableResource.bar");
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testDefinition() {
        ShareableResource rc = new ShareableResource("foo");
        Assert.assertFalse(rc.consumptionDefined(vm1));
        Assert.assertEquals(rc.getVMConsumption(vm1), rc.getDefaultConsumption());

        rc.setVMConsumption(vm1, 3);
        Assert.assertTrue(rc.consumptionDefined(vm1));
        Assert.assertEquals(rc.getVMConsumption(vm1), 3);
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testGets() {
        ShareableResource rc = new ShareableResource("foo");
        List<Integer> ids = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            int id = rnd.nextInt();
            ids.add(id);
            rc.setNodeCapacity(id, i);
        }
        List<Integer> values = rc.getNodesCapacity(ids);
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(values.get(i), (Integer) i);
        }
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testDefined() {
        ShareableResource rc = new ShareableResource("foo");
        List<Integer> ids = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            int id = rnd.nextInt();
            ids.add(id);
            rc.setVMConsumption(id, i);
        }
        Assert.assertTrue(rc.getDefinedVMs().containsAll(ids) && rc.getDefinedVMs().size() == ids.size());
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testUnset() {
        ShareableResource rc = new ShareableResource("foo");
        rc.setVMConsumption(vm1, 3);
        Assert.assertTrue(rc.unsetVM(vm1));
        Assert.assertFalse(rc.consumptionDefined(vm1));

        //Next, id is not defined so not 'unsetable'
        Assert.assertFalse(rc.unsetVM(vm1));

    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testMax() {
        ShareableResource rc = new ShareableResource("foo");
        rc.setVMConsumption(vm1, 3);

        rc.setVMConsumption(vm2, 7);
        Assert.assertEquals(7, rc.max(rc.getDefinedVMs(), true, false));
        Set<Integer> x = new HashSet<>();
        x.add(vm1);
        Assert.assertEquals(3, rc.max(x, true, false));
        rc.setVMConsumption(vm1, -15);
        x.add(vm3);
        Assert.assertEquals(-15, rc.max(x, true, false)); //If the default value would have been counted, it would have return 0
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testMin() {
        ShareableResource rc = new ShareableResource("foo");
        rc.setVMConsumption(vm1, 3);

        rc.setVMConsumption(vm2, 7);
        Assert.assertEquals(3, rc.min(rc.getDefinedVMs(), true, false));
        Set<Integer> x = new HashSet<>();
        x.add(vm2);
        Assert.assertEquals(7, rc.min(x, true, false));
        rc.setVMConsumption(vm2, 18);
        x.add(vm3);
        Assert.assertEquals(18, rc.min(x, true, false)); //If the default value would have been counted, it would have return 0
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testSum() {
        ShareableResource rc = new ShareableResource("foo", -5, -5); //-5 as default no code value to detect its presence in sum (would be an error)

        rc.setVMConsumption(vm1, 3);
        rc.setVMConsumption(vm2, 7);
        Assert.assertEquals(10, rc.sum(rc.getDefinedVMs(), true, false));
        Set<Integer> x = new HashSet<>();
        x.add(vm2);
        Assert.assertEquals(7, rc.sum(x, true, false));
        rc.setVMConsumption(vm2, 18);
        x.clear();
        x.add(vm3);
        Assert.assertEquals(0, rc.sum(x, true, false));
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testToString() {
        ShareableResource rc = new ShareableResource("foo");
        rc.setVMConsumption(vm1, 1);
        rc.setVMConsumption(vm2, 2);
        rc.setVMConsumption(vm3, 3);
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
        ShareableResource rc1 = new ShareableResource("foo", -1, -1);
        rc1.setVMConsumption(vm1, 3);
        rc1.setVMConsumption(vm2, 5);
        ShareableResource rc2 = rc1.clone();
        Assert.assertEquals(rc1, rc2);
        Assert.assertEquals(rc1.hashCode(), rc2.hashCode());

        rc1.setVMConsumption(vm1, -5);
        Assert.assertNotEquals(rc1, rc2);

        rc1.setVMConsumption(vm1, 3);
        Assert.assertEquals(rc1, rc2);

        rc2.unsetVM(vm2);
        Assert.assertNotEquals(rc1, rc2);
    }

    @Test
    public void testSubstitution() {
        ShareableResource rc = new ShareableResource("foo");
        rc.setVMConsumption(vm1, 3);
        Assert.assertTrue(rc.substituteVM(vm1, vm10));
        Assert.assertEquals(rc.getVMConsumption(vm10), 3);
        Assert.assertTrue(rc.substituteVM(vm3, vm7));
        Assert.assertEquals(rc.getVMConsumption(vm7), 0);
    }
}
