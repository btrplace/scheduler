/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.view;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.ModelConverter;
import org.btrplace.json.model.view.network.NetworkConverter;
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
 * Unit tests for {@link NetworkConverter}.
 *
 * @author Vincent Kherbache
 */
public class NetworkConverterTest {

    @Test
    public void switchesAndLinksTest() throws JSONConverterException {

        Model mo = new DefaultModel();
        Network net = new Network();
        Switch s = net.newSwitch(1000);
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addOnlineNode(n2);
        net.connect(1000, s, n1, n2);
        mo.attach(net);

        ModelConverter mc = new ModelConverter();
        JSONObject jo = mc.toJSON(mo);
        System.err.println(jo);
        Model mo2 = mc.fromJSON(jo);
        Network net2 = Network.get(mo2);

        Assert.assertTrue(net.getSwitches().equals(net2.getSwitches()));
        Assert.assertTrue(net.getLinks().equals(net2.getLinks()));
        Assert.assertTrue(net.getConnectedNodes().equals(net2.getConnectedNodes()));
    }

    @Test
    public void staticRoutingTest() throws JSONConverterException {

        Model mo = new DefaultModel();
        Network net = new Network(new StaticRouting());
        Switch s = net.newSwitch(1000);
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addOnlineNode(n2);
        net.connect(1000, s, n1, n2);
        Map<Link, Boolean> route = new LinkedHashMap<>();
        route.put(net.getConnectedLinks(n1).get(0), true);
        route.put(net.getConnectedLinks(n2).get(0), false);
        ((StaticRouting) net.getRouting()).setStaticRoute(new StaticRouting.NodesMap(n1, n2), route);
        mo.attach(net);

        ModelConverter mc = new ModelConverter();
        JSONObject jo = mc.toJSON(mo);
        System.err.println(jo);
        Model mo2 = mc.fromJSON(jo);
        Network net2 = Network.get(mo2);

        Assert.assertTrue(net.getSwitches().equals(net2.getSwitches()));
        Assert.assertTrue(net.getLinks().equals(net2.getLinks()));
        Assert.assertTrue(net.getConnectedNodes().equals(net2.getConnectedNodes()));

        Map<StaticRouting.NodesMap, Map<Link, Boolean>> routes = ((StaticRouting) net.getRouting()).getStaticRoutes();
        Map<StaticRouting.NodesMap, Map<Link, Boolean>> routes2 = ((StaticRouting) net2.getRouting()).getStaticRoutes();
        for (StaticRouting.NodesMap nm : routes.keySet()) {
            for (StaticRouting.NodesMap nm2 : routes2.keySet()) {
                Assert.assertTrue(nm.equals(nm2));
                Assert.assertTrue(routes.get(nm).equals(routes2.get(nm2)));
            }
        }
    }
}
