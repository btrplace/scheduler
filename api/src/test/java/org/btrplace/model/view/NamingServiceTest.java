/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.view;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link org.btrplace.model.view.NamingService}.
 *
 * @author Fabien Hermenier
 */
public class NamingServiceTest {

    @Test
    public void testRegisterAndGets() {
        NamingService<Node> ns = NamingService.newNodeNS();
        Model mo = new DefaultModel();
        Node n = mo.newNode();
        Assert.assertTrue(ns.register(n, "n0"));
        Assert.assertFalse(ns.register(mo.newNode(), "n0"));
        Assert.assertEquals(ns.getElementIdentifier(), Node.TYPE);
        Assert.assertEquals(ns.getNamedElements().size(), 1);
    }

    @Test(dependsOnMethods = {"testRegisterAndGets"})
    public void testResolution() {
        NamingService<VM> ns = NamingService.newVMNS();
        Assert.assertEquals(ns.getElementIdentifier(), VM.TYPE);
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        ns.register(v, "vm0");
        Assert.assertEquals(ns.resolve(v), "vm0");
        Assert.assertEquals(ns.resolve("vm0"), v);
        Assert.assertNull(ns.resolve("vm1"));
    }

    @Test(dependsOnMethods = {"testRegisterAndGets"})
    public void testSubstitution() {
        NamingService<VM> ns = NamingService.newVMNS();
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        ns.register(v, "vm0");
        VM v2 = mo.newVM();
        ns.substituteVM(v, v2);
        Assert.assertNull(ns.resolve(v));
        Assert.assertEquals(ns.resolve(v2), "vm0");
        Assert.assertEquals(ns.resolve("vm0"), v2);

        NamingService<Node> ns2 = NamingService.newNodeNS();
        ns2.register(mo.newNode(), "n0");
        Assert.assertTrue(ns2.substituteVM(mo.newVM(), mo.newVM()));
    }

    @Test(dependsOnMethods = {"testRegisterAndGets", "testResolution"})
    public void testClone() {
        NamingService<Node> ns = NamingService.newNodeNS();
        Model mo = new DefaultModel();
        Node n = mo.newNode();
        NamingService<Node> ns2 = ns.copy();
        Assert.assertTrue(ns2.register(n, "n0"));
        Assert.assertNull(ns.resolve(n));
        Assert.assertNull(ns.resolve("n0"));
    }

    @Test(dependsOnMethods = {"testRegisterAndGets"})
    public void testEqualsAndHashCode() {
        NamingService<VM> ns = NamingService.newVMNS();
        Assert.assertEquals(ns, ns);
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        NamingService<VM> ns2 = NamingService.newVMNS();
        Assert.assertEquals(ns, ns2);
        Assert.assertEquals(ns.hashCode(), ns2.hashCode());
        ns2.register(v, "vm0");
        Assert.assertNotEquals(ns, ns2);
    }

    @Test
    public void testGetViews() {
        Model mo = new DefaultModel();
        Assert.assertNull(NamingService.getVMNames(mo));
        Assert.assertNull(NamingService.getNodeNames(mo));
        NamingService<VM> vmNs = NamingService.newVMNS();
        NamingService<Node> nodeNs = NamingService.newNodeNS();
        mo.attach(vmNs);
        mo.attach(nodeNs);
        Assert.assertEquals(NamingService.getNodeNames(mo), nodeNs);
        Assert.assertEquals(NamingService.getVMNames(mo), vmNs);
    }
}
