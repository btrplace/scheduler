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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link DefaultMapping}.
 *
 * @author Fabien Hermenier
 */
public class DefaultMappingTest {


    /**
     * Create an empty mapping and check all the getters.
     */
    @Test
    public void testInstantiation() {
        Mapping c = new DefaultMapping();

        //Global getters
        Assert.assertTrue(c.getAllNodes().isEmpty());
        Assert.assertTrue(c.getOfflineNodes().isEmpty());
        Assert.assertTrue(c.getOnlineNodes().isEmpty());

        Assert.assertTrue(c.getAllVMs().isEmpty());
        Assert.assertTrue(c.getRunningVMs().isEmpty());
        Assert.assertTrue(c.getRunningVMs(UUID.randomUUID()).isEmpty());

        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertTrue(c.getSleepingVMs(UUID.randomUUID()).isEmpty());

        Assert.assertTrue(c.getReadyVMs().isEmpty());

        Assert.assertNull(c.getVMLocation(UUID.randomUUID()));

        Assert.assertNotNull(c.toString());

        Assert.assertFalse(c.removeVM(UUID.randomUUID()));
        Assert.assertFalse(c.removeNode(UUID.randomUUID()));
        Assert.assertFalse(c.containsNode(UUID.randomUUID()));
        Assert.assertFalse(c.containsVM(UUID.randomUUID()));
    }

    /**
     * Add/remove online node but no state switch
     */
    @Test(dependsOnMethods = {"testInstantiation"})
    public void testOnlineNode() {

        Mapping c = new DefaultMapping();
        UUID n1 = UUID.randomUUID();

        c.addOnlineNode(n1);
        //Basic getters for online
        Assert.assertEquals(c.getAllNodes().size(), 1);
        Assert.assertTrue(c.getAllNodes().contains(n1));
        Assert.assertEquals(c.getOnlineNodes().size(), 1);
        Assert.assertTrue(c.getOnlineNodes().contains(n1));
        Assert.assertTrue(c.getOfflineNodes().isEmpty());

        //Nothing is on the node
        Assert.assertTrue(c.getRunningVMs(n1).isEmpty());
        Assert.assertTrue(c.getSleepingVMs(n1).isEmpty());

        //Double add, fail
        c.addOnlineNode(n1);
        Assert.assertEquals(c.getOnlineNodes().size(), 1);

        Assert.assertTrue(c.removeNode(n1));
        Assert.assertTrue(c.getAllNodes().isEmpty());
        Assert.assertFalse(c.removeNode(n1));
    }

    /**
     * Add/remove offline node but not state switch.
     */
    @Test(dependsOnMethods = {"testInstantiation"})
    public void testOfflineNode() {
        Mapping c = new DefaultMapping();
        //Add an offline node
        UUID n2 = UUID.randomUUID();
        Assert.assertTrue(c.addOfflineNode(n2));
        Assert.assertEquals(1, c.getAllNodes().size());
        Assert.assertTrue(c.getAllNodes().contains(n2));
        Assert.assertEquals(1, c.getOfflineNodes().size());
        Assert.assertTrue(c.getOnlineNodes().isEmpty());
        Assert.assertTrue(c.getOfflineNodes().contains(n2));
    }

    @Test(dependsOnMethods = {"testInstantiation", "testRunningVM", "testSleeping", "testOnlineNode", "testOfflineNode"})
    public void testRemoveNode() {
        Mapping c = new DefaultMapping();
        UUID n1 = UUID.randomUUID();

        //Remove empty online node
        c.addOnlineNode(n1);
        Assert.assertTrue(c.removeNode(n1));
        Assert.assertTrue(c.getAllNodes().isEmpty());
        Assert.assertTrue(c.getOnlineNodes().isEmpty());

        //Remove empty offline node
        c.addOfflineNode(n1);
        Assert.assertTrue(c.removeNode(n1));
        Assert.assertTrue(c.getAllNodes().isEmpty());
        Assert.assertTrue(c.getOnlineNodes().isEmpty());

        //Remove a node running VM. Must fail
        c.addOnlineNode(n1);
        c.addRunningVM(UUID.randomUUID(), n1);
        Assert.assertFalse(c.removeNode(n1));
        Assert.assertEquals(c.getAllNodes().size(), 1);
        Assert.assertTrue(c.getAllNodes().contains(n1));
        Assert.assertEquals(c.getOnlineNodes().size(), 1);
        Assert.assertTrue(c.getOnlineNodes().contains(n1));

        //Remove a node with a sleeping VM on it. Must fail
        UUID n2 = UUID.randomUUID();
        c.addOnlineNode(n2);
        c.addSleepingVM(UUID.randomUUID(), n2);
        Assert.assertFalse(c.removeNode(n2));
        Assert.assertEquals(c.getAllNodes().size(), 2);
        Assert.assertTrue(c.getAllNodes().contains(n2));
        Assert.assertEquals(c.getOnlineNodes().size(), 2);
        Assert.assertTrue(c.getOnlineNodes().contains(n2));
    }

    /**
     * Test the addition/removal of running VM. No state switch
     */
    @Test(dependsOnMethods = {"testOfflineNode", "testOnlineNode"})
    public void testRunningVM() {
        Mapping c = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();

        UUID vm = UUID.randomUUID();
        c.addOnlineNode(n1);
        c.addOfflineNode(n2);

        Assert.assertTrue(c.addRunningVM(vm, n1));
        Assert.assertTrue(c.getRunningVMs().size() == 1 && c.getRunningVMs().contains(vm));
        Assert.assertTrue(c.getRunningVMs(n1).size() == 1 && c.getRunningVMs(n1).contains(vm));
        Assert.assertTrue(c.getAllVMs().size() == 1 && c.getAllVMs().contains(vm));
        Assert.assertTrue(c.getSleepingVMs().isEmpty() && c.getReadyVMs().isEmpty());
        Assert.assertEquals(c.getVMLocation(vm), n1);

        Assert.assertFalse(c.addRunningVM(UUID.randomUUID(), n2));
        Assert.assertEquals(1, c.getAllVMs().size());

        Assert.assertFalse(c.addRunningVM(vm, UUID.randomUUID()));
        Assert.assertEquals(1, c.getAllVMs().size());

        Assert.assertTrue(c.removeVM(vm));
        Assert.assertTrue(c.getAllVMs().isEmpty());

        UUID n3 = UUID.randomUUID();
        c.addOnlineNode(n3);
        UUID v2 = UUID.randomUUID();
        UUID v3 = UUID.randomUUID();
        UUID n4 = UUID.randomUUID();
        c.addOnlineNode(n4);
        c.addRunningVM(v2, n1);
        c.addRunningVM(v3, n4);
        c.addRunningVM(vm, n3);

        Set<UUID> nodes = new HashSet<UUID>();
        nodes.add(n1);
        nodes.add(n3);
        Set<UUID> on = c.getRunningVMs(nodes);
        Assert.assertTrue(on.size() == 2 && on.contains(vm) && on.contains(v2));
    }

    /**
     * Test the addition/removal of sleeping VM. No state switch
     */
    @Test(dependsOnMethods = {"testOfflineNode", "testOnlineNode"})
    public void testSleeping() {
        Mapping c = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();

        UUID vm = UUID.randomUUID();
        c.addOnlineNode(n1);
        c.addOfflineNode(n2);

        Assert.assertTrue(c.addSleepingVM(vm, n1));
        Assert.assertTrue(c.getSleepingVMs().size() == 1 && c.getSleepingVMs().contains(vm));
        Assert.assertTrue(c.getSleepingVMs(n1).size() == 1 && c.getSleepingVMs(n1).contains(vm));
        Assert.assertTrue(c.getAllVMs().size() == 1 && c.getAllVMs().contains(vm));
        Assert.assertTrue(c.getRunningVMs().isEmpty() && c.getReadyVMs().isEmpty());
        Assert.assertEquals(c.getVMLocation(vm), n1);

        Assert.assertFalse(c.addSleepingVM(UUID.randomUUID(), n2));
        Assert.assertEquals(1, c.getAllVMs().size());

        Assert.assertFalse(c.addSleepingVM(vm, UUID.randomUUID()));
        Assert.assertEquals(1, c.getAllVMs().size());

        Assert.assertTrue(c.removeVM(vm));
        Assert.assertTrue(c.getAllVMs().isEmpty());

    }

    /**
     * Test the addition/removal of waiting VM. No state switch
     */
    @Test(dependsOnMethods = {"testInstantiation"})
    public void testWaiting() {
        Mapping c = new DefaultMapping();
        UUID vm = UUID.randomUUID();

        c.addReadyVM(vm);
        Assert.assertTrue(c.getAllVMs().size() == 1 && c.getAllVMs().contains(vm));
        Assert.assertTrue(c.getReadyVMs().size() == 1 && c.getReadyVMs().contains(vm));
        Assert.assertTrue(c.getRunningVMs().isEmpty() && c.getSleepingVMs().isEmpty());
        Assert.assertEquals(c.getVMLocation(vm), null);

        Assert.assertTrue(c.removeVM(vm));
        Assert.assertTrue(c.getAllVMs().isEmpty());
    }

    @Test(dependsOnMethods = {"testInstantiation", "testOfflineNode", "testOnlineNode", "testInstantiation"})
    public void testSwitchNodeState() {
        Mapping c = new DefaultMapping();

        UUID n1 = UUID.randomUUID();

        //Set online then offline then online. Everything is ok
        c.addOnlineNode(n1);
        Assert.assertTrue(c.addOfflineNode(n1));
        Assert.assertTrue(c.getAllNodes().size() == 1 && c.getOfflineNodes().contains(n1) && c.getOnlineNodes().isEmpty());
        c.addOnlineNode(n1);
        Assert.assertTrue(c.getAllNodes().size() == 1 && c.getOnlineNodes().contains(n1) && c.getOfflineNodes().isEmpty());

        //A VM is running on the node, no way it can be turned off
        c.addRunningVM(UUID.randomUUID(), n1);
        Assert.assertFalse(c.addOfflineNode(n1));
        Assert.assertTrue(c.getAllNodes().size() == 1 && c.getOnlineNodes().contains(n1) && c.getOfflineNodes().isEmpty());


        //The same but with a sleeping VM
        UUID n2 = UUID.randomUUID();
        c.addOnlineNode(n2);
        c.addSleepingVM(UUID.randomUUID(), n2);

        Assert.assertFalse(c.addOfflineNode(n1));
        Assert.assertTrue(c.getAllNodes().size() == 2 && c.getOnlineNodes().contains(n2) && c.getOfflineNodes().isEmpty());

    }


    @Test(dependsOnMethods = {"testInstantiation", "testRunningVM"})
    public void testReplaceRunningVM() {
        Mapping c = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID n3 = UUID.randomUUID();
        UUID vm = UUID.randomUUID();
        c.addOnlineNode(n1);
        c.addOnlineNode(n2);
        c.addOfflineNode(n3);

        c.addRunningVM(vm, n1);
        //Replace a running VM to another place
        Assert.assertTrue(c.addRunningVM(vm, n2));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(n1).isEmpty() && c.getRunningVMs(n2).size() == 1 && c.getVMLocation(vm) == n2);

        //Yep, unable to replace as the node is offline
        Assert.assertFalse(c.addRunningVM(vm, n3));
        Assert.assertTrue(c.addRunningVM(vm, n2));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(n1).isEmpty() && c.getRunningVMs(n2).size() == 1 && c.getVMLocation(vm) == n2);

        //From running to sleeping state
        //Stay on the same node but the state change
        Assert.assertTrue(c.addSleepingVM(vm, n2));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(n2).isEmpty() && c.getSleepingVMs(n2).size() == 1 && c.getVMLocation(vm) == n2);

        //On a new node
        Assert.assertTrue(c.removeVM(vm));
        c.addRunningVM(vm, n1);
        Assert.assertTrue(c.addSleepingVM(vm, n2));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(n2).isEmpty() && c.getSleepingVMs(n2).size() == 1 && c.getVMLocation(vm) == n2);

        //From running to waiting state
        c.removeVM(vm);
        c.addRunningVM(vm, n2);
        c.addReadyVM(vm);
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(n2).isEmpty() && c.getVMLocation(vm) == null && c.getReadyVMs().contains(vm));
    }

    @Test(dependsOnMethods = {"testInstantiation", "testSleeping"})
    public void testReplaceSleepingVM() {
        Mapping c = new DefaultMapping();

        UUID n1 = UUID.randomUUID();
        UUID vm = UUID.randomUUID();

        c.addOnlineNode(n1);
        c.addSleepingVM(vm, n1);

        //To run to the same node
        Assert.assertTrue(c.addRunningVM(vm, n1));
        Assert.assertEquals(c.getAllVMs().size(), 1);
        Assert.assertEquals(c.getRunningVMs().size(), 1);
        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertEquals(c.getVMLocation(vm), n1);

        //Run on another node
        c.removeVM(vm);
        c.addSleepingVM(vm, n1);
        UUID n2 = UUID.randomUUID();
        c.addOnlineNode(n2);
        Assert.assertTrue(c.addRunningVM(vm, n2));
        Assert.assertEquals(c.getAllVMs().size(), 1);
        Assert.assertEquals(c.getRunningVMs().size(), 1);
        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertEquals(c.getVMLocation(vm), n2);

        //Sleep somewhere else
        c.clear();
        c.addOnlineNode(n1);
        c.addOnlineNode(n2);
        c.removeVM(vm);
        c.addSleepingVM(vm, n1);
        Assert.assertTrue(c.addSleepingVM(vm, n2));
        Assert.assertEquals(c.getAllVMs().size(), 1);
        Assert.assertTrue(c.getSleepingVMs(n1).isEmpty());
        Assert.assertTrue(c.getSleepingVMs(n2).contains(vm));
        Assert.assertTrue(c.getSleepingVMs().contains(vm));
        Assert.assertEquals(c.getVMLocation(vm), n2);


        //Go waiting
        c.clear();
        c.addOnlineNode(n1);
        c.addSleepingVM(vm, n1);
        c.addReadyVM(vm);
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertEquals(1, c.getReadyVMs().size());
        Assert.assertTrue(c.getAllVMs().contains(vm));
        Assert.assertTrue(c.getSleepingVMs(n1).isEmpty());
    }

    @Test(dependsOnMethods = {"testInstantiation", "testClear", "testWaiting", "testRunningVM", "testSleeping"})
    public void testReplaceWaitingVM() {
        Mapping c = new DefaultMapping();
        UUID vm = UUID.randomUUID();
        c.addReadyVM(vm);
        UUID n = UUID.randomUUID();
        c.addOnlineNode(n);

        //Waiting -> run
        Assert.assertTrue(c.addRunningVM(vm, n));
        Assert.assertTrue(c.getAllVMs().contains(vm));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertEquals(1, c.getRunningVMs(n).size());
        Assert.assertTrue(c.getRunningVMs(n).contains(vm));
        Assert.assertTrue(c.getReadyVMs().isEmpty());

        //Waiting -> sleeping
        c.clear();
        c.addOnlineNode(n);
        c.addReadyVM(vm);
        Assert.assertTrue(c.addSleepingVM(vm, n));
        Assert.assertTrue(c.getAllVMs().contains(vm));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertEquals(1, c.getSleepingVMs(n).size());
        Assert.assertTrue(c.getSleepingVMs(n).contains(vm));
        Assert.assertTrue(c.getReadyVMs().isEmpty());


    }

    @Test
    public void testToString() {
        Mapping c = new DefaultMapping();

        UUID n3 = UUID.randomUUID();
        UUID n4 = UUID.randomUUID();

        UUID n1 = UUID.randomUUID();
        c.addOnlineNode(n1);
        c.addRunningVM(UUID.randomUUID(), n1);
        c.addRunningVM(UUID.randomUUID(), n1);
        c.addSleepingVM(UUID.randomUUID(), n1);

        UUID n2 = UUID.randomUUID();
        c.addOnlineNode(n2);
        c.addSleepingVM(UUID.randomUUID(), n2);
        c.addSleepingVM(UUID.randomUUID(), n2);

        c.addOnlineNode(n3);

        c.addOfflineNode(n4);

        c.addReadyVM(UUID.randomUUID());
        c.addReadyVM(UUID.randomUUID());
        Assert.assertNotNull(c.toString());
    }

    @Test
    public void testClone() {
        Mapping c1 = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID n3 = UUID.randomUUID();

        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        UUID vm4 = UUID.randomUUID();
        UUID vm5 = UUID.randomUUID();

        c1.addOnlineNode(n1);
        c1.addOnlineNode(n2);
        c1.addOfflineNode(n3);
        c1.addReadyVM(vm1);
        c1.addRunningVM(vm2, n1);
        c1.addSleepingVM(vm3, n1);
        c1.addRunningVM(vm4, n2);
        c1.addRunningVM(vm5, n2);

        Mapping c2 = c1.clone();

        Assert.assertEquals(c1, c2);

        UUID lastVM = UUID.randomUUID();
        c1.addReadyVM(lastVM);
        Assert.assertFalse(c1.equals(c2));
        Assert.assertFalse(c2.equals(c1));

        c1.removeVM(lastVM);
        Assert.assertEquals(c1, c2);

    }

    @Test(dependsOnMethods = {"testClone"})
    public void testEquals() {
        Mapping c1 = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID n3 = UUID.randomUUID();

        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        UUID vm4 = UUID.randomUUID();
        UUID vm5 = UUID.randomUUID();

        c1.addOnlineNode(n1);
        c1.addOnlineNode(n2);
        c1.addOfflineNode(n3);
        c1.addReadyVM(vm1);
        c1.addRunningVM(vm2, n1);
        c1.addSleepingVM(vm3, n1);
        c1.addRunningVM(vm4, n2);
        c1.addRunningVM(vm5, n2);

        Mapping c2 = c1.clone();

        Assert.assertEquals(c1, c2);
        Assert.assertEquals(c1.hashCode(), c2.hashCode());

        //Remove a VM, not equals
        c1.removeVM(vm1);
        Assert.assertNotSame(c1, c2);

        //Put the VM elsewhere
        c1 = c2.clone();
        c1.addRunningVM(vm1, n1);
        Assert.assertNotSame(c1, c2);

        //Remove a node
        c1 = c2.clone();
        c1.removeNode(n3);
        Assert.assertNotSame(c1, c2);

        //Move a VM
        c1 = c2.clone();
        c1.addRunningVM(vm4, n1);
        Assert.assertNotSame(c1, c2);

    }

    @Test(dependsOnMethods = {"testInstantiation", "testOnlineNode", "testOfflineNode", "testRunningVM", "testWaiting", "testSleeping"})
    public void testClear() {
        Mapping c = new DefaultMapping();
        c.addOfflineNode(UUID.randomUUID());
        UUID n = UUID.randomUUID();
        c.addOnlineNode(n);
        c.addRunningVM(UUID.randomUUID(), n);
        c.addRunningVM(UUID.randomUUID(), n);
        c.addSleepingVM(UUID.randomUUID(), n);
        c.addReadyVM(UUID.randomUUID());

        c.clear();
        Assert.assertTrue(c.getAllNodes().isEmpty());
        Assert.assertTrue(c.getAllVMs().isEmpty());
        Assert.assertTrue(c.getRunningVMs().isEmpty());
        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertTrue(c.getReadyVMs().isEmpty());
        Assert.assertTrue(c.getOnlineNodes().isEmpty());
        Assert.assertTrue(c.getOfflineNodes().isEmpty());
        Assert.assertTrue(c.getRunningVMs(n).isEmpty());
        Assert.assertTrue(c.getSleepingVMs(n).isEmpty());
    }

    @Test(dependsOnMethods = {"testInstantiation", "testOnlineNode", "testOfflineNode", "testRunningVM", "testWaiting", "testSleeping"})
    public void testClearAllVMs() {
        Mapping c = new DefaultMapping();
        c.addOfflineNode(UUID.randomUUID());
        UUID n = UUID.randomUUID();
        c.addOnlineNode(n);
        c.addRunningVM(UUID.randomUUID(), n);
        c.addRunningVM(UUID.randomUUID(), n);
        c.addSleepingVM(UUID.randomUUID(), n);
        c.addReadyVM(UUID.randomUUID());

        c.clearAllVMs();
        Assert.assertEquals(c.getAllNodes().size(), 2);
        Assert.assertTrue(c.getAllVMs().isEmpty());
        Assert.assertTrue(c.getRunningVMs().isEmpty());
        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertTrue(c.getReadyVMs().isEmpty());
        Assert.assertEquals(c.getOnlineNodes().size(), 1);
        Assert.assertEquals(c.getOfflineNodes().size(), 1);
        Assert.assertTrue(c.getRunningVMs(n).isEmpty());
        Assert.assertTrue(c.getSleepingVMs(n).isEmpty());
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testClearNode() {
        Mapping c = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        c.addOnlineNode(n1);
        c.addOnlineNode(n2);
        c.addRunningVM(UUID.randomUUID(), n1);
        c.addRunningVM(UUID.randomUUID(), n2);
        c.addSleepingVM(UUID.randomUUID(), n1);
        c.addSleepingVM(UUID.randomUUID(), n2);
        c.addReadyVM(UUID.randomUUID());
        c.clearNode(n1);
        Assert.assertEquals(3, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(n1).isEmpty());
        Assert.assertTrue(c.getSleepingVMs(n1).isEmpty());
    }
}
