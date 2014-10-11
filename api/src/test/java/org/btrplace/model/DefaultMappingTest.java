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

package org.btrplace.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Unit tests for {@link DefaultMapping}.
 *
 * @author Fabien Hermenier
 */
public class DefaultMappingTest {

    private static List<VM> vms = Util.newVMs(10);
    private static List<Node> ns = Util.newNodes(10);

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
        Assert.assertTrue(c.getRunningVMs(new Node(11)).isEmpty());

        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertTrue(c.getSleepingVMs(new Node(10)).isEmpty());

        Assert.assertTrue(c.getReadyVMs().isEmpty());

        Assert.assertNull(c.getVMLocation(new VM(11)));

        Assert.assertNotNull(c.toString());

        Assert.assertFalse(c.remove(new Node(1)));
        Assert.assertFalse(c.remove(new VM(1)));
        Assert.assertFalse(c.contains(new Node(1)));
        Assert.assertFalse(c.contains(new VM(1)));

        Assert.assertEquals(c.getNbNodes(), 0);
        Assert.assertEquals(c.getNbVMs(), 0);
    }

    /**
     * Add/remove online node but no state switch
     */
    @Test(dependsOnMethods = {"testInstantiation"})
    public void testOnlineNode() {

        Mapping c = new DefaultMapping();

        c.addOnlineNode(ns.get(0));
        //Basic getters for online
        Assert.assertEquals(c.getAllNodes().size(), 1);
        Assert.assertTrue(c.getAllNodes().contains(ns.get(0)));
        Assert.assertEquals(c.getOnlineNodes().size(), 1);
        Assert.assertTrue(c.isOnline(ns.get(0)));
        Assert.assertTrue(c.getOfflineNodes().isEmpty());
        Assert.assertEquals(c.getNbNodes(), 1);

        //Nothing is on the node
        Assert.assertTrue(c.getRunningVMs(ns.get(0)).isEmpty());
        Assert.assertTrue(c.getSleepingVMs(ns.get(0)).isEmpty());

        //Double add, fail
        c.addOnlineNode(ns.get(0));
        Assert.assertEquals(c.getOnlineNodes().size(), 1);
        Assert.assertEquals(c.getNbNodes(), 1);

        Assert.assertTrue(c.remove(ns.get(0)));
        Assert.assertEquals(c.getNbNodes(), 0);
        Assert.assertTrue(c.getAllNodes().isEmpty());
        Assert.assertFalse(c.remove(ns.get(0)));
        Assert.assertEquals(c.getNbNodes(), 0);
    }

    /**
     * Add/remove offline node but not state switch.
     */
    @Test(dependsOnMethods = {"testInstantiation"})
    public void testOfflineNode() {
        Mapping c = new DefaultMapping();
        //Add an offline node
        Assert.assertTrue(c.addOfflineNode(ns.get(1)));
        Assert.assertEquals(c.getNbNodes(), 1);
        Assert.assertEquals(1, c.getAllNodes().size());
        Assert.assertTrue(c.getAllNodes().contains(ns.get(1)));
        Assert.assertEquals(1, c.getOfflineNodes().size());
        Assert.assertTrue(c.getOnlineNodes().isEmpty());
        Assert.assertTrue(c.isOffline(ns.get(1)));

        //Double add, denied
        Assert.assertTrue(c.addOfflineNode(ns.get(1)));
        Assert.assertEquals(c.getNbNodes(), 1);
        Assert.assertEquals(1, c.getAllNodes().size());

    }

    @Test(dependsOnMethods = {"testInstantiation", "testRunningVM", "testSleeping", "testOnlineNode", "testOfflineNode"})
    public void testRemoveNode() {
        Mapping c = new DefaultMapping();

        //Remove empty online node
        c.addOnlineNode(ns.get(0));
        Assert.assertTrue(c.remove(ns.get(0)));
        Assert.assertTrue(c.getAllNodes().isEmpty());
        Assert.assertTrue(c.getOnlineNodes().isEmpty());
        Assert.assertEquals(c.getNbNodes(), 0);

        //Remove empty offline node
        c.addOfflineNode(ns.get(0));
        Assert.assertEquals(c.getNbNodes(), 1);
        Assert.assertTrue(c.remove(ns.get(0)));
        Assert.assertTrue(c.getAllNodes().isEmpty());
        Assert.assertTrue(c.getOnlineNodes().isEmpty());
        Assert.assertEquals(c.getNbNodes(), 0);

        //Remove a node running VM. Must fail
        c.addOnlineNode(ns.get(0));
        Assert.assertEquals(c.getNbNodes(), 1);
        c.addRunningVM(new VM(15), ns.get(0));
        Assert.assertFalse(c.remove(ns.get(0)));
        Assert.assertEquals(c.getNbNodes(), 1);
        Assert.assertEquals(c.getAllNodes().size(), 1);
        Assert.assertTrue(c.getAllNodes().contains(ns.get(0)));
        Assert.assertEquals(c.getOnlineNodes().size(), 1);
        Assert.assertTrue(c.isOnline(ns.get(0)));

        //Remove a node with a sleeping VM on it. Must fail
        c.addOnlineNode(ns.get(1));
        Assert.assertEquals(c.getNbNodes(), 2);
        c.addSleepingVM(new VM(15), ns.get(1));
        Assert.assertFalse(c.remove(ns.get(1)));
        Assert.assertEquals(c.getNbNodes(), 2);
        Assert.assertEquals(c.getAllNodes().size(), 2);
        Assert.assertTrue(c.getAllNodes().contains(ns.get(1)));
        Assert.assertEquals(c.getOnlineNodes().size(), 2);
        Assert.assertTrue(c.isOnline(ns.get(1)));
    }

    /**
     * Test the addition/removal of running VM. No state switch
     */
    @Test(dependsOnMethods = {"testOfflineNode", "testOnlineNode"})
    public void testRunningVM() {
        Mapping c = new DefaultMapping();
        c.addOnlineNode(ns.get(0));
        c.addOfflineNode(ns.get(1));

        Assert.assertTrue(c.addRunningVM(vms.get(0), ns.get(0)));
        Assert.assertTrue(c.getRunningVMs().size() == 1 && c.isRunning(vms.get(0)));
        Assert.assertEquals(c.getNbVMs(), 1);
        Assert.assertTrue(c.getRunningVMs(ns.get(0)).size() == 1 && c.getRunningVMs(ns.get(0)).contains(vms.get(0)));
        Assert.assertTrue(c.getAllVMs().size() == 1 && c.getAllVMs().contains(vms.get(0)));
        Assert.assertTrue(c.getSleepingVMs().isEmpty() && c.getReadyVMs().isEmpty());
        Assert.assertEquals(c.getVMLocation(vms.get(0)), ns.get(0));

        Assert.assertFalse(c.addRunningVM(new VM(15), ns.get(1)));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertEquals(c.getNbVMs(), 1);

        Assert.assertFalse(c.addRunningVM(vms.get(0), new Node(15)));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertEquals(c.getNbVMs(), 1);

        Assert.assertTrue(c.remove(vms.get(0)));
        Assert.assertTrue(c.getAllVMs().isEmpty());
        Assert.assertEquals(c.getNbVMs(), 0);

        c.addOnlineNode(ns.get(2));
        c.addOnlineNode(ns.get(3));
        c.addRunningVM(vms.get(1), ns.get(0));
        c.addRunningVM(vms.get(2), ns.get(3));
        c.addRunningVM(vms.get(0), ns.get(2));
        Assert.assertEquals(c.getNbVMs(), 3);

        Set<Node> nodes = new HashSet<>();
        nodes.add(ns.get(0));
        nodes.add(ns.get(2));
        Set<VM> on = c.getRunningVMs(nodes);
        Assert.assertTrue(on.size() == 2 && on.contains(vms.get(0)) && on.contains(vms.get(1)));
    }

    /**
     * Test the addition/removal of sleeping VM. No state switch
     */
    @Test(dependsOnMethods = {"testOfflineNode", "testOnlineNode"})
    public void testSleeping() {
        Mapping c = new DefaultMapping();
        c.addOnlineNode(ns.get(0));
        c.addOfflineNode(ns.get(1));

        Assert.assertTrue(c.addSleepingVM(vms.get(0), ns.get(0)));
        Assert.assertTrue(c.getSleepingVMs().size() == 1 && c.isSleeping(vms.get(0)));
        Assert.assertEquals(c.getNbVMs(), 1);
        Assert.assertTrue(c.getSleepingVMs(ns.get(0)).size() == 1 && c.getSleepingVMs(ns.get(0)).contains(vms.get(0)));
        Assert.assertTrue(c.getAllVMs().size() == 1 && c.getAllVMs().contains(vms.get(0)));
        Assert.assertTrue(c.getRunningVMs().isEmpty() && c.getReadyVMs().isEmpty());
        Assert.assertEquals(c.getVMLocation(vms.get(0)), ns.get(0));

        Assert.assertFalse(c.addSleepingVM(new VM(15), ns.get(1)));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertEquals(c.getNbVMs(), 1);

        Assert.assertFalse(c.addSleepingVM(vms.get(0), new Node(20)));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertEquals(c.getNbVMs(), 1);

        Assert.assertTrue(c.remove(vms.get(0)));
        Assert.assertTrue(c.getAllVMs().isEmpty());
        Assert.assertEquals(c.getNbVMs(), 0);

    }

    /**
     * Test the addition/removal of waiting VM. No state switch
     */
    @Test(dependsOnMethods = {"testInstantiation"})
    public void testWaiting() {
        Mapping c = new DefaultMapping();
        c.addReadyVM(vms.get(0));
        Assert.assertTrue(c.getAllVMs().size() == 1 && c.getAllVMs().contains(vms.get(0)));
        Assert.assertTrue(c.getReadyVMs().size() == 1 && c.isReady(vms.get(0)));
        Assert.assertTrue(c.getRunningVMs().isEmpty() && c.getSleepingVMs().isEmpty());
        Assert.assertNull(c.getVMLocation(vms.get(0)));
        Assert.assertEquals(c.getNbVMs(), 1);

        Assert.assertTrue(c.remove(vms.get(0)));
        Assert.assertTrue(c.getAllVMs().isEmpty());
        Assert.assertEquals(c.getNbVMs(), 0);
    }

    @Test(dependsOnMethods = {"testInstantiation", "testOfflineNode", "testOnlineNode", "testInstantiation"})
    public void testSwitchNodeState() {
        Mapping c = new DefaultMapping();

        //Set online then offline then online. Everything is ok
        c.addOnlineNode(ns.get(0));
        Assert.assertTrue(c.addOfflineNode(ns.get(0)));
        Assert.assertEquals(c.getNbNodes(), 1);
        Assert.assertTrue(c.getAllNodes().size() == 1 && c.isOffline(ns.get(0)) && c.getOnlineNodes().isEmpty());
        c.addOnlineNode(ns.get(0));
        Assert.assertTrue(c.getAllNodes().size() == 1 && c.isOnline(ns.get(0)) && c.getOfflineNodes().isEmpty());
        Assert.assertEquals(c.getNbNodes(), 1);
        //A VM is running on the node, no way it can be turned off
        c.addRunningVM(vms.get(0), ns.get(0));
        Assert.assertFalse(c.addOfflineNode(ns.get(0)));
        Assert.assertTrue(c.getAllNodes().size() == 1 && c.isOnline(ns.get(0)) && c.getOfflineNodes().isEmpty());
        Assert.assertEquals(c.getNbNodes(), 1);


        //The same but with a sleeping VM
        c.addOnlineNode(ns.get(1));
        c.addSleepingVM(vms.get(0), ns.get(1));

        Assert.assertFalse(c.addOfflineNode(ns.get(1)));
        Assert.assertEquals(c.getNbNodes(), 2);
        Assert.assertTrue(c.getAllNodes().size() == 2 && c.isOnline(ns.get(1)) && c.getOfflineNodes().isEmpty());

        c.remove(vms.get(0));
        Assert.assertTrue(c.addOfflineNode(ns.get(1)));
        Assert.assertEquals(c.getNbNodes(), 2);

    }


    @Test(dependsOnMethods = {"testInstantiation", "testRunningVM"})
    public void testReplaceRunningVM() {
        Mapping c = new DefaultMapping();
        c.addOnlineNode(ns.get(0));
        c.addOnlineNode(ns.get(1));
        c.addOfflineNode(ns.get(2));

        c.addRunningVM(vms.get(0), ns.get(0));
        Assert.assertEquals(c.getNbVMs(), 1);
        //Replace a running VM to another place
        Assert.assertTrue(c.addRunningVM(vms.get(0), ns.get(1)));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(ns.get(0)).isEmpty() && c.getRunningVMs(ns.get(1)).size() == 1 && c.getVMLocation(vms.get(0)) == ns.get(1));
        Assert.assertEquals(c.getNbVMs(), 1);

        //Yep, unable to replace as the node is offline
        Assert.assertFalse(c.addRunningVM(vms.get(0), ns.get(2)));
        Assert.assertTrue(c.addRunningVM(vms.get(0), ns.get(1)));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(ns.get(0)).isEmpty() && c.getRunningVMs(ns.get(1)).size() == 1 && c.getVMLocation(vms.get(0)) == ns.get(1));
        Assert.assertEquals(c.getNbVMs(), 1);

        //From running to sleeping state
        //Stay on the same node but the state change
        Assert.assertTrue(c.addSleepingVM(vms.get(0), ns.get(1)));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(ns.get(1)).isEmpty() && c.getSleepingVMs(ns.get(1)).size() == 1 && c.getVMLocation(vms.get(0)) == ns.get(1));
        Assert.assertEquals(c.getNbVMs(), 1);

        //On a new node
        Assert.assertTrue(c.remove(vms.get(0)));
        c.addRunningVM(vms.get(0), ns.get(0));
        Assert.assertTrue(c.addSleepingVM(vms.get(0), ns.get(1)));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(ns.get(1)).isEmpty() && c.getSleepingVMs(ns.get(1)).size() == 1 && c.getVMLocation(vms.get(0)) == ns.get(1));
        Assert.assertEquals(c.getNbVMs(), 1);

        //From running to waiting state
        c.remove(vms.get(0));
        c.addRunningVM(vms.get(0), ns.get(1));
        c.addReadyVM(vms.get(0));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertTrue(c.getRunningVMs(ns.get(1)).isEmpty() && c.getVMLocation(vms.get(0)) == null && c.isReady(vms.get(0)));
        Assert.assertEquals(c.getNbVMs(), 1);
    }

    @Test(dependsOnMethods = {"testInstantiation", "testSleeping"})
    public void testReplaceSleepingVM() {
        Mapping c = new DefaultMapping();

        c.addOnlineNode(ns.get(0));
        c.addSleepingVM(vms.get(0), ns.get(0));
        Assert.assertEquals(c.getNbVMs(), 1);

        //To run to the same node
        Assert.assertTrue(c.addRunningVM(vms.get(0), ns.get(0)));
        Assert.assertEquals(c.getAllVMs().size(), 1);
        Assert.assertEquals(c.getRunningVMs().size(), 1);
        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertEquals(c.getVMLocation(vms.get(0)), ns.get(0));
        Assert.assertEquals(c.getNbVMs(), 1);

        //Run on another node
        c.remove(vms.get(0));
        c.addSleepingVM(vms.get(0), ns.get(0));
        c.addOnlineNode(ns.get(1));
        Assert.assertTrue(c.addRunningVM(vms.get(0), ns.get(1)));
        Assert.assertEquals(c.getAllVMs().size(), 1);
        Assert.assertEquals(c.getRunningVMs().size(), 1);
        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertEquals(c.getVMLocation(vms.get(0)), ns.get(1));
        Assert.assertEquals(c.getNbVMs(), 1);

        //Sleep somewhere else
        c.clear();
        c.addOnlineNode(ns.get(0));
        c.addOnlineNode(ns.get(1));
        c.remove(vms.get(0));
        c.addSleepingVM(vms.get(0), ns.get(0));
        Assert.assertTrue(c.addSleepingVM(vms.get(0), ns.get(1)));
        Assert.assertEquals(c.getAllVMs().size(), 1);
        Assert.assertTrue(c.getSleepingVMs(ns.get(0)).isEmpty());
        Assert.assertTrue(c.getSleepingVMs(ns.get(1)).contains(vms.get(0)));
        Assert.assertTrue(c.isSleeping(vms.get(0)));
        Assert.assertEquals(c.getVMLocation(vms.get(0)), ns.get(1));
        Assert.assertEquals(c.getNbVMs(), 1);


        //Go waiting
        c.clear();
        c.addOnlineNode(ns.get(0));
        c.addSleepingVM(vms.get(0), ns.get(0));
        c.addReadyVM(vms.get(0));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertEquals(1, c.getReadyVMs().size());
        Assert.assertTrue(c.getAllVMs().contains(vms.get(0)));
        Assert.assertTrue(c.getSleepingVMs(ns.get(0)).isEmpty());
        Assert.assertEquals(c.getNbVMs(), 1);
    }

    @Test(dependsOnMethods = {"testInstantiation", "testClear", "testWaiting", "testRunningVM", "testSleeping"})
    public void testReplaceWaitingVM() {
        Mapping c = new DefaultMapping();
        c.addReadyVM(vms.get(0));
        c.addOnlineNode(ns.get(0));
        Assert.assertEquals(c.getNbVMs(), 1);

        //Waiting -> run
        Assert.assertTrue(c.addRunningVM(vms.get(0), ns.get(0)));
        Assert.assertTrue(c.getAllVMs().contains(vms.get(0)));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertEquals(c.getNbVMs(), 1);
        Assert.assertEquals(1, c.getRunningVMs(ns.get(0)).size());
        Assert.assertTrue(c.getRunningVMs(ns.get(0)).contains(vms.get(0)));
        Assert.assertTrue(c.getReadyVMs().isEmpty());

        //Waiting -> sleeping
        c.clear();
        c.addOnlineNode(ns.get(0));
        c.addReadyVM(vms.get(0));
        Assert.assertTrue(c.addSleepingVM(vms.get(0), ns.get(0)));
        Assert.assertTrue(c.getAllVMs().contains(vms.get(0)));
        Assert.assertEquals(1, c.getAllVMs().size());
        Assert.assertEquals(1, c.getSleepingVMs(ns.get(0)).size());
        Assert.assertTrue(c.getSleepingVMs(ns.get(0)).contains(vms.get(0)));
        Assert.assertTrue(c.getReadyVMs().isEmpty());
        Assert.assertEquals(c.getNbVMs(), 1);

    }

    @Test
    public void testToString() {
        Mapping c = new DefaultMapping();

        c.addOnlineNode(ns.get(0));
        c.addRunningVM(vms.get(0), ns.get(0));
        c.addRunningVM(vms.get(1), ns.get(0));
        c.addSleepingVM(vms.get(2), ns.get(0));

        c.addOnlineNode(ns.get(1));
        c.addSleepingVM(vms.get(3), ns.get(1));
        c.addSleepingVM(vms.get(4), ns.get(1));

        c.addOnlineNode(ns.get(2));

        c.addOfflineNode(ns.get(3));

        c.addReadyVM(vms.get(5));
        c.addReadyVM(vms.get(6));
        Assert.assertNotNull(c.toString());
    }

    @Test
    public void testClone() {
        Mapping c1 = new DefaultMapping();

        c1.addOnlineNode(ns.get(0));
        c1.addOnlineNode(ns.get(1));
        c1.addOfflineNode(ns.get(2));
        c1.addReadyVM(vms.get(0));
        c1.addRunningVM(vms.get(1), ns.get(0));
        c1.addSleepingVM(vms.get(2), ns.get(0));
        c1.addRunningVM(vms.get(3), ns.get(1));
        c1.addRunningVM(vms.get(4), ns.get(1));

        Mapping c2 = c1.clone();

        Assert.assertEquals(c1, c2);

        c1.addReadyVM(vms.get(5));
        Assert.assertFalse(c1.equals(c2));
        Assert.assertFalse(c2.equals(c1));

        c1.remove(vms.get(5));
        Assert.assertEquals(c1, c2);

    }

    @Test(dependsOnMethods = {"testClone"})
    public void testEquals() {
        Mapping c1 = new DefaultMapping();

        c1.addOnlineNode(ns.get(0));
        c1.addOnlineNode(ns.get(1));
        c1.addOfflineNode(ns.get(2));
        c1.addReadyVM(vms.get(0));
        c1.addRunningVM(vms.get(1), ns.get(0));
        c1.addSleepingVM(vms.get(2), ns.get(0));
        c1.addRunningVM(vms.get(3), ns.get(1));
        c1.addRunningVM(vms.get(4), ns.get(1));

        Mapping c2 = c1.clone();

        Assert.assertEquals(c1, c2);
        Assert.assertEquals(c1.hashCode(), c2.hashCode());

        //Remove a VM, not equals
        c1.remove(vms.get(0));
        Assert.assertNotSame(c1, c2);

        //Put the VM elsewhere
        c1 = c2.clone();
        c1.addRunningVM(vms.get(0), ns.get(0));
        Assert.assertNotSame(c1, c2);

        //Remove a node
        c1 = c2.clone();
        c1.remove(ns.get(2));
        Assert.assertNotSame(c1, c2);

        //Move a VM
        c1 = c2.clone();
        c1.addRunningVM(vms.get(3), ns.get(0));
        Assert.assertNotSame(c1, c2);

    }

    @Test(dependsOnMethods = {"testInstantiation", "testOnlineNode", "testOfflineNode", "testRunningVM", "testWaiting", "testSleeping"})
    public void testClear() {
        Mapping c = new DefaultMapping();
        c.addOfflineNode(ns.get(1));
        c.addOnlineNode(ns.get(0));
        c.addRunningVM(vms.get(0), ns.get(0));
        c.addRunningVM(vms.get(1), ns.get(0));
        c.addSleepingVM(vms.get(2), ns.get(0));
        c.addReadyVM(vms.get(3));

        c.clear();
        Assert.assertEquals(c.getNbNodes(), 0);
        Assert.assertEquals(c.getNbVMs(), 0);
        Assert.assertTrue(c.getAllNodes().isEmpty());
        Assert.assertTrue(c.getAllVMs().isEmpty());
        Assert.assertTrue(c.getRunningVMs().isEmpty());
        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertTrue(c.getReadyVMs().isEmpty());
        Assert.assertTrue(c.getOnlineNodes().isEmpty());
        Assert.assertTrue(c.getOfflineNodes().isEmpty());
        Assert.assertTrue(c.getRunningVMs(ns.get(0)).isEmpty());
        Assert.assertTrue(c.getSleepingVMs(ns.get(0)).isEmpty());
    }

    @Test(dependsOnMethods = {"testInstantiation", "testOnlineNode", "testOfflineNode", "testRunningVM", "testWaiting", "testSleeping"})
    public void testClearAllVMs() {
        Mapping c = new DefaultMapping();
        c.addOfflineNode(ns.get(0));
        c.addOnlineNode(ns.get(1));
        c.addRunningVM(vms.get(0), ns.get(1));
        c.addRunningVM(vms.get(1), ns.get(1));
        c.addSleepingVM(vms.get(2), ns.get(1));
        c.addReadyVM(vms.get(3));

        c.clearAllVMs();
        Assert.assertEquals(c.getAllNodes().size(), 2);
        Assert.assertTrue(c.getAllVMs().isEmpty());
        Assert.assertTrue(c.getRunningVMs().isEmpty());
        Assert.assertTrue(c.getSleepingVMs().isEmpty());
        Assert.assertTrue(c.getReadyVMs().isEmpty());
        Assert.assertEquals(c.getOnlineNodes().size(), 1);
        Assert.assertEquals(c.getOfflineNodes().size(), 1);
        Assert.assertTrue(c.getRunningVMs(ns.get(1)).isEmpty());
        Assert.assertTrue(c.getSleepingVMs(ns.get(1)).isEmpty());
        Assert.assertEquals(c.getNbVMs(), 0);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testClearNode() {
        Mapping c = new DefaultMapping();
        c.addOnlineNode(ns.get(0));
        c.addOnlineNode(ns.get(1));
        c.addRunningVM(vms.get(0), ns.get(0));
        c.addRunningVM(vms.get(1), ns.get(1));
        c.addSleepingVM(vms.get(2), ns.get(0));
        c.addSleepingVM(vms.get(3), ns.get(1));
        c.addReadyVM(vms.get(4));
        c.clearNode(ns.get(0));
        Assert.assertEquals(3, c.getAllVMs().size());
        Assert.assertEquals(c.getNbVMs(), 3);
        Assert.assertTrue(c.getRunningVMs(ns.get(0)).isEmpty());
        Assert.assertTrue(c.getSleepingVMs(ns.get(0)).isEmpty());
    }

    @Test
    public void testGetRunningVMsOnOfflineNodes() {
        Mapping m = new DefaultMapping();
        m.addOnlineNode(ns.get(0));
        m.addOnlineNode(ns.get(1));
        m.addRunningVM(vms.get(0), ns.get(0));
        m.addRunningVM(vms.get(1), ns.get(0));
        m.addRunningVM(vms.get(2), ns.get(1));
        m.addRunningVM(vms.get(3), ns.get(1));

        m.addOfflineNode(ns.get(2));
        Assert.assertTrue(m.getRunningVMs(ns.get(2)).isEmpty());
        Set<Node> nodes = new HashSet<>();
        nodes.add(ns.get(2));
        Assert.assertTrue(m.getRunningVMs(nodes).isEmpty());
    }
}
