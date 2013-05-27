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

package btrplace.model;

import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;


/**
 * Unit tests for {@link DefaultMapping}.
 *
 * @author Fabien Hermenier
 */
public class DefaultMappingTest implements PremadeElements {

    private static Random rnd = new Random();

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
        Assert.assertTrue(c.getRunningVMs(rnd.nextInt()).isEmpty());

        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertTrue(c.getSleepingVMs(rnd.nextInt()).isEmpty());

        Assert.assertTrue(c.getReadyVMs().isEmpty());

        Assert.assertEquals(c.getVMLocation(rnd.nextInt()), -1);

        Assert.assertNotNull(c.toString());

        Assert.assertFalse(c.removeVM(rnd.nextInt()));
        Assert.assertFalse(c.removeNode(rnd.nextInt()));
        Assert.assertFalse(c.containsNode(rnd.nextInt()));
        Assert.assertFalse(c.containsVM(rnd.nextInt()));
    }

    /**
     * Add/remove online node but no state switch
     */
    @Test(dependsOnMethods = {"testInstantiation"})
    public void testOnlineNode() {

        Mapping c = new DefaultMapping();

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
        c.addRunningVM(rnd.nextInt(), n1);
        Assert.assertFalse(c.removeNode(n1));
        Assert.assertEquals(c.getAllNodes().size(), 1);
        Assert.assertTrue(c.getAllNodes().contains(n1));
        Assert.assertEquals(c.getOnlineNodes().size(), 1);
        Assert.assertTrue(c.getOnlineNodes().contains(n1));

        //Remove a node with a sleeping VM on it. Must fail
        c.addOnlineNode(n2);
        c.addSleepingVM(rnd.nextInt(), n2);
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
        c.addOnlineNode(n1);
        c.addOfflineNode(n2);

        Assert.assertTrue(c.addRunningVM(vm1, n1));
        Assert.assertTrue(c.getRunningVMs().size() == 1 && c.getRunningVMs().contains(vm1));
        Assert.assertTrue(c.getRunningVMs(n1).size() == 1 && c.getRunningVMs(n1).contains(vm1));
        Assert.assertTrue(c.getAllVMs().size() == 1 && c.getAllVMs().contains(vm1));
        Assert.assertTrue(c.getSleepingVMs().isEmpty() && c.getReadyVMs().isEmpty());
        Assert.assertEquals(c.getVMLocation(vm1), n1);

        Assert.assertFalse(c.addRunningVM(rnd.nextInt(), n2));
        Assert.assertEquals(1, c.getAllVMs().size());

        Assert.assertFalse(c.addRunningVM(vm1, rnd.nextInt()));
        Assert.assertEquals(1, c.getAllVMs().size());

        Assert.assertTrue(c.removeVM(vm1));
        Assert.assertTrue(c.getAllVMs().isEmpty());

        c.addOnlineNode(n3);
        c.addOnlineNode(n4);
        c.addRunningVM(vm2, n1);
        c.addRunningVM(vm3, n4);
        c.addRunningVM(vm1, n3);

        Set<Integer> nodes = new HashSet<>();
        nodes.add(n1);
        nodes.add(n3);
        Set<Integer> on = c.getRunningVMs(nodes);
        Assert.assertTrue(on.size() == 2 && on.contains(vm1) && on.contains(vm2));
    }

    /**
     * Test the addition/removal of sleeping VM. No state switch
     */
    @Test(dependsOnMethods = {"testOfflineNode", "testOnlineNode"})
    public void testSleeping() {
        Mapping c = new DefaultMapping();
        c.addOnlineNode(n1);
        c.addOfflineNode(n2);

        Assert.assertTrue(c.addSleepingVM(vm1, n1));
        Assert.assertTrue(c.getSleepingVMs().size() == 1 && c.getSleepingVMs().contains(vm1));
        Assert.assertTrue(c.getSleepingVMs(n1).size() == 1 && c.getSleepingVMs(n1).contains(vm1));
        Assert.assertTrue(c.getAllVMs().size() == 1 && c.getAllVMs().contains(vm1));
        Assert.assertTrue(c.getRunningVMs().isEmpty() && c.getReadyVMs().isEmpty());
        Assert.assertEquals(c.getVMLocation(vm1), n1);

        Assert.assertFalse(c.addSleepingVM(rnd.nextInt(), n2));
        Assert.assertEquals(1, c.getAllVMs().size());

        Assert.assertFalse(c.addSleepingVM(vm1, rnd.nextInt()));
        Assert.assertEquals(1, c.getAllVMs().size());

        Assert.assertTrue(c.removeVM(vm1));
        Assert.assertTrue(c.getAllVMs().isEmpty());

    }

    /**
     * Test the addition/removal of waiting VM. No state switch
     */
    @Test(dependsOnMethods = {"testInstantiation"})
    public void testWaiting() {
        Mapping c = new DefaultMapping();
        c.addReadyVM(vm1);
        Assert.assertTrue(c.getAllVMs().size() == 1 && c.getAllVMs().contains(vm1));
        Assert.assertTrue(c.getReadyVMs().size() == 1 && c.getReadyVMs().contains(vm1));
        Assert.assertTrue(c.getRunningVMs().isEmpty() && c.getSleepingVMs().isEmpty());
        Assert.assertEquals(c.getVMLocation(vm1), -1);

        Assert.assertTrue(c.removeVM(vm1));
        Assert.assertTrue(c.getAllVMs().isEmpty());
    }

    @Test(dependsOnMethods = {"testInstantiation", "testOfflineNode", "testOnlineNode", "testInstantiation"})
    public void testSwitchNodeState() {
        Mapping c = new DefaultMapping();


        //Set online then offline then online. Everything is ok
        c.addOnlineNode(n1);
        Assert.assertTrue(c.addOfflineNode(n1));
        Assert.assertTrue(c.getAllNodes().size() == 1 && c.getOfflineNodes().contains(n1) && c.getOnlineNodes().isEmpty());
        c.addOnlineNode(n1);
        Assert.assertTrue(c.getAllNodes().size() == 1 && c.getOnlineNodes().contains(n1) && c.getOfflineNodes().isEmpty());

        //A VM is running on the node, no way it can be turned off
        c.addRunningVM(vm1, n1);
        Assert.assertFalse(c.addOfflineNode(n1));
        Assert.assertTrue(c.getAllNodes().size() == 1 && c.getOnlineNodes().contains(n1) && c.getOfflineNodes().isEmpty());


        //The same but with a sleeping VM
        c.addOnlineNode(n2);
        c.addSleepingVM(vm1, n2);

        Assert.assertFalse(c.addOfflineNode(n2));
        Assert.assertTrue(c.getAllNodes().size() == 2 && c.getOnlineNodes().contains(n2) && c.getOfflineNodes().isEmpty());

    }


    @Test(dependsOnMethods = {"testInstantiation", "testRunningVM"})
    public void testReplaceRunningVM() {
        Mapping c = new DefaultMapping();
        c.addOnlineNode(n1);
        c.addOnlineNode(n2);
        c.addOfflineNode(n3);

        c.addRunningVM(vm1, n1);
        //Replace a running VM to another place
        Assert.assertTrue(c.addRunningVM(vm1, n2));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(n1).isEmpty() && c.getRunningVMs(n2).size() == 1 && c.getVMLocation(vm1) == n2);

        //Yep, unable to replace as the node is offline
        Assert.assertFalse(c.addRunningVM(vm1, n3));
        Assert.assertTrue(c.addRunningVM(vm1, n2));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(n1).isEmpty() && c.getRunningVMs(n2).size() == 1 && c.getVMLocation(vm1) == n2);

        //From running to sleeping state
        //Stay on the same node but the state change
        Assert.assertTrue(c.addSleepingVM(vm1, n2));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(n2).isEmpty() && c.getSleepingVMs(n2).size() == 1 && c.getVMLocation(vm1) == n2);

        //On a new node
        Assert.assertTrue(c.removeVM(vm1));
        c.addRunningVM(vm1, n1);
        Assert.assertTrue(c.addSleepingVM(vm1, n2));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(n2).isEmpty() && c.getSleepingVMs(n2).size() == 1 && c.getVMLocation(vm1) == n2);

        //From running to waiting state
        c.removeVM(vm1);
        c.addRunningVM(vm1, n2);
        c.addReadyVM(vm1);
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(n2).isEmpty() && c.getVMLocation(vm1) < 0 && c.getReadyVMs().contains(vm1));
    }

    @Test(dependsOnMethods = {"testInstantiation", "testSleeping"})
    public void testReplaceSleepingVM() {
        Mapping c = new DefaultMapping();

        c.addOnlineNode(n1);
        c.addSleepingVM(vm1, n1);

        //To run to the same node
        Assert.assertTrue(c.addRunningVM(vm1, n1));
        Assert.assertEquals(c.getAllVMs().size(), 1);
        Assert.assertEquals(c.getRunningVMs().size(), 1);
        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertEquals(c.getVMLocation(vm1), n1);

        //Run on another node
        c.removeVM(vm1);
        c.addSleepingVM(vm1, n1);
        c.addOnlineNode(n2);
        Assert.assertTrue(c.addRunningVM(vm1, n2));
        Assert.assertEquals(c.getAllVMs().size(), 1);
        Assert.assertEquals(c.getRunningVMs().size(), 1);
        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertEquals(c.getVMLocation(vm1), n2);

        //Sleep somewhere else
        c.clear();
        c.addOnlineNode(n1);
        c.addOnlineNode(n2);
        c.removeVM(vm1);
        c.addSleepingVM(vm1, n1);
        Assert.assertTrue(c.addSleepingVM(vm1, n2));
        Assert.assertEquals(c.getAllVMs().size(), 1);
        Assert.assertTrue(c.getSleepingVMs(n1).isEmpty());
        Assert.assertTrue(c.getSleepingVMs(n2).contains(vm1));
        Assert.assertTrue(c.getSleepingVMs().contains(vm1));
        Assert.assertEquals(c.getVMLocation(vm1), n2);


        //Go waiting
        c.clear();
        c.addOnlineNode(n1);
        c.addSleepingVM(vm1, n1);
        c.addReadyVM(vm1);
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertEquals(1, c.getReadyVMs().size());
        Assert.assertTrue(c.getAllVMs().contains(vm1));
        Assert.assertTrue(c.getSleepingVMs(n1).isEmpty());
    }

    @Test(dependsOnMethods = {"testInstantiation", "testClear", "testWaiting", "testRunningVM", "testSleeping"})
    public void testReplaceWaitingVM() {
        Mapping c = new DefaultMapping();
        c.addReadyVM(vm1);
        c.addOnlineNode(n1);

        //Waiting -> run
        Assert.assertTrue(c.addRunningVM(vm1, n1));
        Assert.assertTrue(c.getAllVMs().contains(vm1));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertEquals(1, c.getRunningVMs(n1).size());
        Assert.assertTrue(c.getRunningVMs(n1).contains(vm1));
        Assert.assertTrue(c.getReadyVMs().isEmpty());

        //Waiting -> sleeping
        c.clear();
        c.addOnlineNode(n1);
        c.addReadyVM(vm1);
        Assert.assertTrue(c.addSleepingVM(vm1, n1));
        Assert.assertTrue(c.getAllVMs().contains(vm1));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertEquals(1, c.getSleepingVMs(n1).size());
        Assert.assertTrue(c.getSleepingVMs(n1).contains(vm1));
        Assert.assertTrue(c.getReadyVMs().isEmpty());


    }

    @Test
    public void testToString() {
        Mapping c = new DefaultMapping();

        c.addOnlineNode(n1);
        c.addRunningVM(vm1, n1);
        c.addRunningVM(vm2, n1);
        c.addSleepingVM(vm3, n1);

        c.addOnlineNode(n2);
        c.addSleepingVM(vm4, n2);
        c.addSleepingVM(vm5, n2);

        c.addOnlineNode(n3);

        c.addOfflineNode(n4);

        c.addReadyVM(vm6);
        c.addReadyVM(vm7);
        Assert.assertNotNull(c.toString());
    }

    @Test
    public void testClone() {
        Mapping c1 = new DefaultMapping();

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

        c1.addReadyVM(vm10);
        Assert.assertFalse(c1.equals(c2));
        Assert.assertFalse(c2.equals(c1));

        c1.removeVM(vm10);
        Assert.assertEquals(c1, c2);

    }

    @Test(dependsOnMethods = {"testClone"})
    public void testEquals() {
        Mapping c1 = new DefaultMapping();

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
        c.addOfflineNode(n2);
        c.addOnlineNode(n1);
        c.addRunningVM(vm1, n1);
        c.addRunningVM(vm2, n1);
        c.addSleepingVM(vm3, n1);
        c.addReadyVM(vm4);

        c.clear();
        Assert.assertTrue(c.getAllNodes().isEmpty());
        Assert.assertTrue(c.getAllVMs().isEmpty());
        Assert.assertTrue(c.getRunningVMs().isEmpty());
        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertTrue(c.getReadyVMs().isEmpty());
        Assert.assertTrue(c.getOnlineNodes().isEmpty());
        Assert.assertTrue(c.getOfflineNodes().isEmpty());
        Assert.assertTrue(c.getRunningVMs(n1).isEmpty());
        Assert.assertTrue(c.getSleepingVMs(n1).isEmpty());
    }

    @Test(dependsOnMethods = {"testInstantiation", "testOnlineNode", "testOfflineNode", "testRunningVM", "testWaiting", "testSleeping"})
    public void testClearAllVMs() {
        Mapping c = new DefaultMapping();
        c.addOfflineNode(n1);
        c.addOnlineNode(n2);
        c.addRunningVM(vm1, n2);
        c.addRunningVM(vm2, n2);
        c.addSleepingVM(vm3, n2);
        c.addReadyVM(vm4);

        c.clearAllVMs();
        Assert.assertEquals(c.getAllNodes().size(), 2);
        Assert.assertTrue(c.getAllVMs().isEmpty());
        Assert.assertTrue(c.getRunningVMs().isEmpty());
        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertTrue(c.getReadyVMs().isEmpty());
        Assert.assertEquals(c.getOnlineNodes().size(), 1);
        Assert.assertEquals(c.getOfflineNodes().size(), 1);
        Assert.assertTrue(c.getRunningVMs(n2).isEmpty());
        Assert.assertTrue(c.getSleepingVMs(n2).isEmpty());
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testClearNode() {
        Mapping c = new DefaultMapping();
        c.addOnlineNode(n1);
        c.addOnlineNode(n2);
        c.addRunningVM(vm1, n1);
        c.addRunningVM(vm2, n2);
        c.addSleepingVM(vm3, n1);
        c.addSleepingVM(vm4, n2);
        c.addReadyVM(vm5);
        c.clearNode(n1);
        Assert.assertEquals(3, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(n1).isEmpty());
        Assert.assertTrue(c.getSleepingVMs(n1).isEmpty());
    }

    @Test
    public void testGetRunningVMsOnOfflineNodes() {
        Mapping m = new DefaultMapping();
        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm2, n1);
        m.addRunningVM(vm3, n2);
        m.addRunningVM(vm4, n2);

        m.addOfflineNode(n3);
        Assert.assertTrue(m.getRunningVMs(n3).isEmpty());
        Set<Integer> ns = new HashSet<>();
        ns.add(n3);
        Assert.assertTrue(m.getRunningVMs(ns).isEmpty());
    }
}
