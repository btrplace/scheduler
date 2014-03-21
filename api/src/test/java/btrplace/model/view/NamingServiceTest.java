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

package btrplace.model.view;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link btrplace.model.view.NamingService}.
 *
 * @author Fabien Hermenier
 */
public class NamingServiceTest {

    @Test
    public void testRegisterAndGets() {
        NamingService ns = new NamingService();
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Node n = mo.newNode();
        Assert.assertTrue(ns.register(v, "vm0"));
        Assert.assertTrue(ns.register(n, "n0"));
        Assert.assertFalse(ns.register(mo.newVM(), "vm0"));

        Assert.assertEquals(ns.getRegisteredElements().size(), 2);
        Assert.assertEquals(ns.getRegisteredNames().size(), 2);
    }

    @Test(dependsOnMethods = {"testRegisterAndGets"})
    public void testResolution() {
        NamingService ns = new NamingService();
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        ns.register(v, "vm0");
        Assert.assertEquals(ns.resolve(v), "vm0");
        Assert.assertEquals(ns.resolve("vm0"), v);
        Assert.assertNull(ns.resolve(mo.newNode()));
        Assert.assertNull(ns.resolve("vm1"));
    }

    @Test(dependsOnMethods = {"testRegisterAndGets"})
    public void testSubstitution() {
        NamingService ns = new NamingService();
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        ns.register(v, "vm0");
        VM v2 = mo.newVM();
        ns.substituteVM(v, v2);
        Assert.assertNull(ns.resolve(v));
        Assert.assertEquals(ns.resolve(v2), "vm0");
        Assert.assertEquals(ns.resolve("vm0"), v2);
    }

    @Test
    public void testAttach() {
        Model mo = new DefaultModel();
        NamingService ns = new NamingService();
        mo.attach(ns);
        Assert.assertEquals(mo.getView(NamingService.ID), ns);
    }

    @Test(dependsOnMethods = {"testRegisterAndGets", "testResolution"})
    public void testClone() {
        NamingService ns = new NamingService();
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        Node n = mo.newNode();
        ns.register(v, "vm0");
        NamingService ns2 = ns.clone();
        Assert.assertTrue(ns2.register(n, "n0"));
        Assert.assertNull(ns.resolve(n));
        Assert.assertNull(ns.resolve("n0"));
        VM vm2 = mo.newVM();
        ns2.substituteVM(v, vm2);
        Assert.assertNull(ns.resolve(vm2));
    }

    @Test(dependsOnMethods = {"testRegisterAndGets"})
    public void testEqualsAndHashCode() {
        NamingService ns = new NamingService();
        Assert.assertEquals(ns, ns);
        Model mo = new DefaultModel();
        VM v = mo.newVM();
        NamingService ns2 = new NamingService();
        Assert.assertEquals(ns, ns2);
        Assert.assertEquals(ns.hashCode(), ns2.hashCode());
        ns2.register(v, "vm0");
        Assert.assertNotEquals(ns, ns2);
    }
}
