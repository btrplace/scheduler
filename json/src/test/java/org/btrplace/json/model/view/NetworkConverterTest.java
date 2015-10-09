package org.btrplace.json.model.view;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.ModelConverter;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.view.network.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link org.btrplace.json.model.view.NetworkConverter}.
 *
 * @author Vincent Kherbache
 */
public class NetworkConverterTest {
    
    @Test
    public void switchesAndLinksTest() throws JSONConverterException {
        
        Model mo = new DefaultModel();
        Network net = new Network();
        Switch s = net.newSwitch(1000);
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        net.connect(1000, s, n1, n2);
        mo.attach(net);
        
        ModelConverter mc = new ModelConverter();
        JSONObject jo = mc.toJSON(mo);
        Model mo2 = mc.fromJSON(jo);
        Network net2 = (Network) mo2.getView(Network.VIEW_ID);

        Assert.assertTrue(net.getSwitches().equals(net2.getSwitches()));
        Assert.assertTrue(net.getLinks().equals(net2.getLinks()));
        Assert.assertTrue(net.getConnectedNodes().equals(net2.getConnectedNodes()));
    }

    @Test
    public void staticRoutingTest() throws JSONConverterException {

        Model mo = new DefaultModel();
        Network net = new Network(new StaticRouting());
        Switch s = net.newSwitch(1000);
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        net.connect(1000, s, n1, n2);
        ((StaticRouting)net.getRouting()).setStaticRoute(new Routing.NodesMap(n1,n2), net.getLinks());
        mo.attach(net);

        ModelConverter mc = new ModelConverter();
        JSONObject jo = mc.toJSON(mo);
        Model mo2 = mc.fromJSON(jo);
        Network net2 = (Network) mo2.getView(Network.VIEW_ID);

        Assert.assertTrue(net.getSwitches().equals(net2.getSwitches()));
        Assert.assertTrue(net.getLinks().equals(net2.getLinks()));
        Assert.assertTrue(net.getConnectedNodes().equals(net2.getConnectedNodes()));

        Map<Routing.NodesMap, List<Link>> routes = ((StaticRouting) net.getRouting()).getStaticRoutes();
        Map<Routing.NodesMap, List<Link>> routes2 = ((StaticRouting) net2.getRouting()).getStaticRoutes();
        for (Routing.NodesMap nm : routes.keySet()) { for (Routing.NodesMap nm2 : routes2.keySet()) {
                Assert.assertTrue(nm.equals(nm2));
                Assert.assertTrue(routes.get(nm).equals(routes2.get(nm2)));
        }}
    }
}
