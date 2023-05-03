/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.view;

import org.btrplace.model.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link ShareableResource}.
 *
 * @author Fabien Hermenier
 */
public class ShareableResourceTest {

  private static final Model mo = new DefaultModel();
  private static final List<VM> vms = Util.newVMs(mo, 10);
  private static final List<Node> nodes = Util.newNodes(mo, 10);

  @Test
  public void testInstantiation() {
    ShareableResource rc = new ShareableResource("foo");
    Assert.assertEquals(rc.getIdentifier(), "ShareableResource.foo");
    Assert.assertEquals(rc.getResourceIdentifier(), "foo");
    Assert.assertEquals(rc.getDefaultCapacity(), ShareableResource.DEFAULT_NO_VALUE);
    Assert.assertEquals(rc.getDefaultConsumption(), ShareableResource.DEFAULT_NO_VALUE);

    rc = new ShareableResource("bar", 7, 3);
        Assert.assertEquals(rc.getIdentifier(), "ShareableResource.bar");
    }

    @Test
    public void testGet() {
        Model mo = new DefaultModel();
        Assert.assertNull(ShareableResource.get(mo, "cpu"));
        ShareableResource cpu = new ShareableResource("cpu");
        ShareableResource mem = new ShareableResource("mem");
        mo.attach(cpu);
        Assert.assertEquals(ShareableResource.get(mo, "cpu"), cpu);
        Assert.assertNull(ShareableResource.get(mo, "mem"));
        mo.attach(mem);
        Assert.assertEquals(ShareableResource.get(mo, "cpu"), cpu);
        Assert.assertEquals(ShareableResource.get(mo, "mem"), mem);
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

        rc.setConsumption(12, vms.toArray(new VM[0]));
        rc.setCapacity(12, nodes.toArray(new Node[0]));
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testUnset() {
        ShareableResource rc = new ShareableResource("foo");
        rc.setConsumption(vms.get(0), 3);
        rc.unset(vms.get(0));
        Assert.assertFalse(rc.consumptionDefined(vms.get(0)));


        rc.setCapacity(nodes.get(0), 3);
        rc.unset(nodes.get(0));
        Assert.assertFalse(rc.capacityDefined(nodes.get(0)));

        rc.unset(nodes.toArray(new Node[0]));
        rc.unset(vms.toArray(new VM[0]));
        for (final Node no : nodes) {
            Assert.assertFalse(rc.capacityDefined(no));
        }
        for (final VM vm : vms) {
            Assert.assertFalse(rc.consumptionDefined(vm));
        }
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition"})
    public void testSum() {
        ShareableResource rc = new ShareableResource("foo", 5, 5); //-5 as default no code value to detect its presence in sum (would be an error)

        rc.setConsumption(vms.get(0), 3);
        rc.setConsumption(vms.get(1), 7);
        Assert.assertEquals(rc.sumConsumptions(vms.subList(0, 2), false), 10);
        Set<VM> x = new HashSet<>();
        x.add(vms.get(1));
        Assert.assertEquals(rc.sumConsumptions(x, false), 7);
        rc.setConsumption(vms.get(1), 18);
        x.clear();
        x.add(vms.get(2));
        Assert.assertEquals(rc.sumConsumptions(x, false), 0);

        rc.setCapacity(nodes.get(0), 3);
        rc.setCapacity(nodes.get(1), 6);
        Assert.assertEquals(rc.sumCapacities(nodes.subList(0, 2), false), 9);

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
    }

    @Test(dependsOnMethods = {"testInstantiation", "testDefinition", "testEqualsAndHashCode"})
    public void testClone() {
        ShareableResource rc1 = new ShareableResource("foo", 1, 1);
        rc1.setConsumption(vms.get(0), 3);
        rc1.setConsumption(vms.get(1), 5);
        rc1.setCapacity(nodes.get(0), 10);
        rc1.setCapacity(nodes.get(1), 20);
        ShareableResource rc2 = rc1.copy();
        Assert.assertEquals(rc1, rc2);
        Assert.assertEquals(rc1.hashCode(), rc2.hashCode());

        rc1.setConsumption(vms.get(0), 5);
        Assert.assertNotEquals(rc1, rc2);
        rc1.unset(vms.get(0));
        Assert.assertNotEquals(rc1, rc2);
        rc1.setConsumption(vms.get(0), 3);
        Assert.assertEquals(rc1, rc2);

        rc1.setCapacity(nodes.get(0), 5);
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
