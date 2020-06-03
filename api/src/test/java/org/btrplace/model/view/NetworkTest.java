/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.view;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.view.network.Link;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.StaticRouting;
import org.btrplace.model.view.network.Switch;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unit tests for {@link org.btrplace.model.view.network.Network}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.model.view.network.Network
 */
public class NetworkTest {

    /**
     * Test the instantiation and the creation of the objects using the default routing implementation.
     */
    @Test
    public void defaultTest() {

        Model mo = new DefaultModel();
        Network net = new Network();
        Switch s = net.newSwitch(1000);
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        net.connect(2000, s, n1, n2);
        Assert.assertNull(Network.get(mo));

        mo.attach(net);
        Assert.assertEquals(Network.get(mo), net);
        Assert.assertTrue(net.getSwitches().size() == 1);
        Assert.assertEquals(net.getSwitches().get(0), s);
        Assert.assertTrue(s.getCapacity() == 1000);
        Assert.assertTrue(net.getLinks().size() == 2);
        Assert.assertTrue(net.getLinks().size() == 2);
        for (Link l : net.getLinks()) {
            Assert.assertTrue(l.getCapacity() == 2000);
            Assert.assertTrue(l.getSwitch().equals(s) || l.getElement() instanceof Switch);
        }

        Assert.assertTrue(net.getRouting().getPath(n1, n2).size() == 2);
        Assert.assertTrue(net.getRouting().getPath(n1, n2).containsAll(net.getLinks()));
    }

    /**
     * Test the static routing implementation.
     */
    @Test
    public void staticRoutingTest() {

        Model mo = new DefaultModel();
        Network net = new Network(new StaticRouting());
        Switch s = net.newSwitch(1000);
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        net.connect(2000, s, n1, n2);
        Map<Link, Boolean> route = new LinkedHashMap<>();
        route.put(net.getConnectedLinks(n1).get(0), true);
        route.put(net.getConnectedLinks(n2).get(0), false);
        ((StaticRouting) net.getRouting()).setStaticRoute(new StaticRouting.NodesMap(n1, n2), route);
        mo.attach(net);

        Assert.assertTrue(net.getRouting().getPath(n1, n2).size() == 2);
        Assert.assertTrue(net.getRouting().getPath(n1, n2).containsAll(net.getLinks()));
    }
}
