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
        Assert.assertEquals(ns.getElementIdentifier(), "node");
        Assert.assertEquals(ns.getNamedElements().size(), 1);
    }

    @Test(dependsOnMethods = {"testRegisterAndGets"})
    public void testResolution() {
        NamingService<VM> ns = NamingService.newVMNS();
        Assert.assertEquals(ns.getElementIdentifier(), "vm");
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
        NamingService<Node> ns2 = ns.clone();
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
}
