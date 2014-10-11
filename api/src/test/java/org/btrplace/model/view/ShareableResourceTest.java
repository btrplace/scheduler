/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.model.view;

import org.btrplace.model.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Unit tests for {@link ShareableResource}.
 *
 * @author Fabien Hermenier
 */
public class ShareableResourceTest {

    private static Random rnd = new Random();

    private static Model mo = new DefaultModel();
    private static List<VM> vms = Util.newVMs(mo, 10);
    private static List<Node> nodes = Util.newNodes(mo, 10);

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
        Assert.assertFalse(rc.consumptionDefined(vms.get(0)));
        Assert.assertEquals(rc.getConsumption(vms.get(0)), rc.getDefaultConsumption());

        rc.setConsumption(vms.get(0), 3);
        Assert.assertTrue(rc.consumptionDefined(vms.get(0)));
        Assert.assertEquals(rc.getConsumption(vms.get(0)), 3);

        Assert.assertFalse(rc.capacityDefined(nodes.get(0)));
        Assert.assertEquals(rc.getCapacity(nodes.get(0)), rc.getDefaultCapacity());

        rc.setCapacity(nodes.get(0), 3);
        Assert.assertTrue(rc.capacityDefined(nodes.get(0)));
        Assert.assertEquals(rc.getCapacity(nodes.get(0)), 3);

    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testGets() {
        ShareableResource rc = new ShareableResource("foo");
        for (int i = 0; i < 10; i++) {
            rc.setCapacity(nodes.get(i), i);
        }
        List<Integer> values = rc.getCapacities(nodes);
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(values.get(i), (Integer) i);
        }

        for (int i = 0; i < 10; i++) {
            rc.setConsumption(vms.get(i), i);
        }
        values = rc.getConsumptions(vms);
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(values.get(i), (Integer) i);
        }

    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testDefined() {
        ShareableResource rc = new ShareableResource("foo");
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Node n = mo.newNode();
        rc.setConsumption(v, v.id());
        rc.setCapacity(n, n.id());
        Assert.assertTrue(rc.capacityDefined(n));
        Assert.assertTrue(rc.consumptionDefined(v));
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testUnset() {
        ShareableResource rc = new ShareableResource("foo");
        rc.setConsumption(vms.get(0), 3);
        Assert.assertTrue(rc.unset(vms.get(0)));
        Assert.assertFalse(rc.consumptionDefined(vms.get(0)));

        Assert.assertFalse(rc.unset(vms.get(0)));

        rc.setCapacity(nodes.get(0), 3);
        Assert.assertTrue(rc.unset(nodes.get(0)));
        Assert.assertFalse(rc.capacityDefined(nodes.get(0)));

        Assert.assertFalse(rc.unset(nodes.get(0)));

    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testSum() {
        ShareableResource rc = new ShareableResource("foo", -5, -5); //-5 as default no code value to detect its presence in sum (would be an error)

        rc.setConsumption(vms.get(0), 3);
        rc.setConsumption(vms.get(1), 7);
        Assert.assertEquals(10, rc.sumConsumptions(rc.getDefinedVMs(), false));
        Set<VM> x = new HashSet<>();
        x.add(vms.get(1));
        Assert.assertEquals(7, rc.sumConsumptions(x, false));
        rc.setConsumption(vms.get(1), 18);
        x.clear();
        x.add(vms.get(2));
        Assert.assertEquals(0, rc.sumConsumptions(x, false));

        rc.setCapacity(nodes.get(0), 3);
        rc.setCapacity(nodes.get(1), 6);
        Assert.assertEquals(9, rc.sumCapacities(rc.getDefinedNodes(), false));

    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testToString() {
        ShareableResource rc = new ShareableResource("foo");
        rc.setConsumption(vms.get(0), 1);
        rc.setConsumption(vms.get(1), 2);
        rc.setConsumption(vms.get(2), 3);
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
        rc1.setConsumption(vms.get(0), 3);
        rc1.setConsumption(vms.get(1), 5);
        rc1.setCapacity(nodes.get(0), 10);
        rc1.setCapacity(nodes.get(1), 20);
        ShareableResource rc2 = rc1.clone();
        Assert.assertEquals(rc1, rc2);
        Assert.assertEquals(rc1.hashCode(), rc2.hashCode());

        rc1.setConsumption(vms.get(0), -5);
        Assert.assertNotEquals(rc1, rc2);
        rc1.unset(vms.get(0));
        Assert.assertNotEquals(rc1, rc2);
        rc1.setConsumption(vms.get(0), 3);
        Assert.assertEquals(rc1, rc2);

        rc1.setCapacity(nodes.get(0), -5);
        Assert.assertNotEquals(rc1, rc2);
        rc1.unset(nodes.get(0));
        Assert.assertNotEquals(rc1, rc2);
        rc1.setCapacity(nodes.get(0), 10);
        Assert.assertEquals(rc1, rc2);
    }

    @Test
    public void testSubstitution() {
        ShareableResource rc = new ShareableResource("foo");
        rc.setConsumption(vms.get(0), 3);
        Assert.assertTrue(rc.substituteVM(vms.get(0), vms.get(0)));
        Assert.assertEquals(rc.getConsumption(vms.get(0)), 3);
        Assert.assertTrue(rc.substituteVM(vms.get(2), vms.get(6)));
        Assert.assertEquals(rc.getConsumption(vms.get(6)), 0);
    }
}
