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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link DefaultAttributes}.
 *
 * @author Fabien Hermenier
 */
public class DefaultAttributesTest {

    private static Model mo = new DefaultModel();
    private static List<VM> vms = Util.newVMs(mo, 10);
    private static List<Node> nodes = Util.newNodes(mo, 10);

    @Test
    public void testInstantiation() {
        Attributes attrs = new DefaultAttributes();
        Assert.assertFalse(attrs.toString().contains("null"));
        Assert.assertTrue(attrs.getDefined().isEmpty());
    }


    @Test(dependsOnMethods = {"testInstantiation"})
    public void testPutAndGetString() {
        Attributes attrs = new DefaultAttributes();

        Assert.assertFalse(attrs.put(vms.get(0), "foo", "bar"));
        Assert.assertEquals(attrs.getString(vms.get(0), "foo"), "bar");
        Assert.assertTrue(attrs.put(vms.get(0), "foo", "baz"));
        Assert.assertEquals(attrs.getString(vms.get(0), "foo"), "baz");

        Assert.assertNull(attrs.getString(vms.get(0), "__"));
    }


    @Test(dependsOnMethods = {"testInstantiation"})
    public void testPutAndGetDouble() {
        Attributes attrs = new DefaultAttributes();

        Assert.assertFalse(attrs.put(vms.get(0), "foo", 17.3));
        Assert.assertEquals(attrs.getDouble(vms.get(0), "foo"), 17.3);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testPutAndGetBoolean() {
        Attributes attrs = new DefaultAttributes();

        Assert.assertFalse(attrs.put(vms.get(0), "foo", true));
        Assert.assertEquals(attrs.getBoolean(vms.get(0), "foo"), Boolean.TRUE);
        Assert.assertTrue(attrs.put(vms.get(0), "foo", false));
        Assert.assertEquals(attrs.getBoolean(vms.get(0), "foo"), Boolean.FALSE);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testCastAndPut() {
        DefaultAttributes attrs = new DefaultAttributes();

        attrs.castAndPut(vms.get(0), "foo", "foo");
        Assert.assertEquals(attrs.get(vms.get(0), "foo").getClass(), String.class);
        attrs.castAndPut(vms.get(0), "foo", "true");
        Assert.assertEquals(attrs.get(vms.get(0), "foo").getClass(), Boolean.class);

        attrs.castAndPut(vms.get(0), "foo", "false");
        Assert.assertEquals(attrs.get(vms.get(0), "foo").getClass(), Boolean.class);

        attrs.castAndPut(vms.get(0), "foo", "True");
        Assert.assertEquals(attrs.get(vms.get(0), "foo").getClass(), Boolean.class);

        attrs.castAndPut(vms.get(0), "foo", "135");
        Assert.assertEquals(attrs.get(vms.get(0), "foo").getClass(), Integer.class);

        attrs.castAndPut(vms.get(0), "foo", "13.56");
        Assert.assertEquals(attrs.get(vms.get(0), "foo").getClass(), Double.class);
    }

    @Test(dependsOnMethods = {"testPutAndGetString", "testInstantiation"})
    public void testIsSet() {
        Attributes attrs = new DefaultAttributes();

        Assert.assertFalse(attrs.isSet(vms.get(0), "foo"));
        attrs.put(vms.get(0), "foo", "bar");
        Assert.assertTrue(attrs.isSet(vms.get(0), "foo"));
    }

    @Test(dependsOnMethods = {"testPutAndGetString", "testInstantiation"})
    public void testUnset() {
        Attributes attrs = new DefaultAttributes();

        Assert.assertFalse(attrs.unset(vms.get(0), "foo"));
        attrs.put(vms.get(0), "foo", "bar");
        Assert.assertTrue(attrs.unset(vms.get(0), "foo"));
        Assert.assertFalse(attrs.isSet(vms.get(0), "foo"));
        Assert.assertFalse(attrs.unset(vms.get(0), "foo"));
    }

    @Test(dependsOnMethods = {"testInstantiation", "testUnset"})
    public void testClone() {
        Attributes attrs = new DefaultAttributes();
        List<Node> l = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Node u = mo.newNode();
            attrs.put(u, Integer.toString(i), i);
            l.add(u);
        }
        Attributes attrs2 = attrs.clone();

        attrs.unset(l.get(0), "0");
        Assert.assertEquals((int) attrs2.getInteger(l.get(0), "0"), 0);

        attrs2.unset(l.get(1), "1");
        Assert.assertEquals((int) attrs.getInteger(l.get(1), "1"), 1);
    }

    @Test(dependsOnMethods = {"testInstantiation", "testUnset", "testClone"})
    public void testEqualsHashCode() {
        Attributes attrs = new DefaultAttributes();
        for (int i = 0; i < 5; i++) {
            attrs.put(nodes.get(0), Integer.toString(i), i);
            attrs.put(vms.get(0), Integer.toString(i), i);
        }
        Assert.assertTrue(attrs.equals(attrs));
        Attributes attrs2 = attrs.clone();
        Assert.assertTrue(attrs2.equals(attrs));
        Assert.assertTrue(attrs.equals(attrs));
        Assert.assertEquals(attrs.hashCode(), attrs2.hashCode());
        attrs.unset(nodes.get(0), "0");
        Assert.assertFalse(attrs2.equals(attrs));
        Assert.assertFalse(attrs.equals(attrs2));
        Assert.assertNotSame(attrs.hashCode(), attrs2.hashCode());
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testClear() {
        Attributes attrs = new DefaultAttributes();
        for (int i = 0; i < 5; i++) {
            attrs.put(nodes.get(i), Integer.toString(i), i);
            attrs.put(vms.get(i), Integer.toString(i), i);
        }
        attrs.clear();
        Assert.assertTrue(attrs.getDefined().isEmpty());
    }

    @Test
    public void testGetKeys() {
        Attributes attrs = new DefaultAttributes();
        VM u = vms.get(0);
        attrs.put(u, "foo", 1);
        attrs.put(u, "bar", 1);
        Set<String> s = attrs.getKeys(u);
        Assert.assertEquals(s.size(), 2);
        Assert.assertTrue(s.containsAll(Arrays.asList("foo", "bar")));
        Assert.assertEquals(attrs.getKeys(mo.newVM()).size(), 0);
        Assert.assertEquals(attrs.getKeys(mo.newNode()).size(), 0);
    }
}
