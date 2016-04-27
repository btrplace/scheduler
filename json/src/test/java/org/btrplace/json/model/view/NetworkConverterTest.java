/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

import java.util.List;
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
        ((StaticRouting) net.getRouting()).setStaticRoute(new StaticRouting.NodesMap(n1, n2), net.getLinks());
        mo.attach(net);

        ModelConverter mc = new ModelConverter();
        JSONObject jo = mc.toJSON(mo);
        System.err.println(jo);
        Model mo2 = mc.fromJSON(jo);
        Network net2 = Network.get(mo2);

        Assert.assertTrue(net.getSwitches().equals(net2.getSwitches()));
        Assert.assertTrue(net.getLinks().equals(net2.getLinks()));
        Assert.assertTrue(net.getConnectedNodes().equals(net2.getConnectedNodes()));

        Map<StaticRouting.NodesMap, List<Link>> routes = ((StaticRouting) net.getRouting()).getStaticRoutes();
        Map<StaticRouting.NodesMap, List<Link>> routes2 = ((StaticRouting) net2.getRouting()).getStaticRoutes();
        for (StaticRouting.NodesMap nm : routes.keySet()) {
            for (StaticRouting.NodesMap nm2 : routes2.keySet()) {
                Assert.assertTrue(nm.equals(nm2));
                Assert.assertTrue(routes.get(nm).equals(routes2.get(nm2)));
        }}
    }
}
