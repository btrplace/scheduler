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

import java.util.*;

/**
 * Unit tests for {@link DefaultAttributes}.
 *
 * @author Fabien Hermenier
 */
public class DefaultAttributesTest implements PremadeElements {

    private static Random rnd = new Random();

    @Test
    public void testInstantiation() {
        Attributes attrs = new DefaultAttributes();
        Assert.assertFalse(attrs.toString().contains("null"));
        Assert.assertTrue(attrs.getElements().isEmpty());
    }


    @Test(dependsOnMethods = {"testInstantiation"})
    public void testPutAndGetString() {
        Attributes attrs = new DefaultAttributes();

        Assert.assertFalse(attrs.put(vm1, "foo", "bar"));
        Assert.assertEquals(attrs.getString(vm1, "foo"), "bar");
        Assert.assertTrue(attrs.put(vm1, "foo", "baz"));
        Assert.assertEquals(attrs.getString(vm1, "foo"), "baz");

        Assert.assertNull(attrs.getString(vm1, "__"));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testPutAndGetLong() {
        Attributes attrs = new DefaultAttributes();

        Assert.assertFalse(attrs.put(vm1, "foo", 17L));
        Assert.assertEquals(attrs.getLong(vm1, "foo").longValue(), 17L);
        Assert.assertTrue(attrs.put(vm1, "foo", 24L));
        Assert.assertEquals(attrs.getLong(vm1, "foo").intValue(), 24);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testPutAndGetDouble() {
        Attributes attrs = new DefaultAttributes();

        Assert.assertFalse(attrs.put(vm1, "foo", 17.3));
        Assert.assertEquals(attrs.getDouble(vm1, "foo"), 17.3);
        Assert.assertTrue(attrs.put(vm1, "foo", 24L));
        Assert.assertEquals(attrs.getLong(vm1, "foo").longValue(), 24L);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testPutAndGetBoolean() {
        Attributes attrs = new DefaultAttributes();

        Assert.assertFalse(attrs.put(vm1, "foo", true));
        Assert.assertEquals(attrs.getBoolean(vm1, "foo"), Boolean.TRUE);
        Assert.assertTrue(attrs.put(vm1, "foo", false));
        Assert.assertEquals(attrs.getBoolean(vm1, "foo"), Boolean.FALSE);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testCastAndPut() {
        DefaultAttributes attrs = new DefaultAttributes();

        attrs.castAndPut(vm1, "foo", "foo");
        Assert.assertEquals(attrs.get(vm1, "foo").getClass(), String.class);
        attrs.castAndPut(vm1, "foo", "true");
        Assert.assertEquals(attrs.get(vm1, "foo").getClass(), Boolean.class);

        attrs.castAndPut(vm1, "foo", "false");
        Assert.assertEquals(attrs.get(vm1, "foo").getClass(), Boolean.class);

        attrs.castAndPut(vm1, "foo", "True");
        Assert.assertEquals(attrs.get(vm1, "foo").getClass(), Boolean.class);

        attrs.castAndPut(vm1, "foo", "135");
        Assert.assertEquals(attrs.get(vm1, "foo").getClass(), Long.class);

        attrs.castAndPut(vm1, "foo", "13.56");
        Assert.assertEquals(attrs.get(vm1, "foo").getClass(), Double.class);
    }

    @Test(dependsOnMethods = {"testPutAndGetString", "testInstantiation"})
    public void testIsSet() {
        Attributes attrs = new DefaultAttributes();

        Assert.assertFalse(attrs.isSet(vm1, "foo"));
        attrs.put(vm1, "foo", "bar");
        Assert.assertTrue(attrs.isSet(vm1, "foo"));
    }

    @Test(dependsOnMethods = {"testPutAndGetString", "testInstantiation"})
    public void testUnset() {
        Attributes attrs = new DefaultAttributes();

        Assert.assertFalse(attrs.unset(vm1, "foo"));
        attrs.put(vm1, "foo", "bar");
        Assert.assertTrue(attrs.unset(vm1, "foo"));
        Assert.assertFalse(attrs.isSet(vm1, "foo"));
        Assert.assertFalse(attrs.unset(vm1, "foo"));
    }

    @Test(dependsOnMethods = {"testPutAndGetLong", "testInstantiation", "testUnset"})
    public void testClone() {
        Attributes attrs = new DefaultAttributes();
        List<Integer> l = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int u = rnd.nextInt();
            attrs.put(u, Integer.toString(i), i);
            l.add(u);
        }
        Attributes attrs2 = attrs.clone();

        attrs.unset(l.get(0), "0");
        Assert.assertEquals(attrs2.getLong(l.get(0), "0").longValue(), 0);

        attrs2.unset(l.get(1), "1");
        Assert.assertEquals(attrs.getLong(l.get(1), "1").longValue(), 1);
    }

    @Test(dependsOnMethods = {"testPutAndGetLong", "testInstantiation", "testUnset", "testClone"})
    public void testEqualsHashCode() {
        Attributes attrs = new DefaultAttributes();
        List<Integer> l = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int u = rnd.nextInt();
            attrs.put(u, Integer.toString(i), i);
            l.add(u);
        }
        Assert.assertTrue(attrs.equals(attrs));
        Attributes attrs2 = attrs.clone();
        Assert.assertTrue(attrs2.equals(attrs));
        Assert.assertTrue(attrs.equals(attrs));
        Assert.assertEquals(attrs.hashCode(), attrs2.hashCode());
        attrs.unset(l.get(0), "0");
        Assert.assertFalse(attrs2.equals(attrs));
        Assert.assertFalse(attrs.equals(attrs2));
        Assert.assertNotSame(attrs.hashCode(), attrs2.hashCode());
    }

    @Test(dependsOnMethods = {"testInstantiation", "testPutAndGetLong"})
    public void testClear() {
        Attributes attrs = new DefaultAttributes();
        for (int i = 0; i < 5; i++) {
            int u = rnd.nextInt();
            attrs.put(u, Integer.toString(i), i);
        }
        attrs.clear();
        Assert.assertTrue(attrs.getElements().isEmpty());
    }

    @Test
    public void testGetKeys() {
        Attributes attrs = new DefaultAttributes();
        int u = 1;
        attrs.put(u, "foo", 1);
        attrs.put(u, "bar", 1);
        Set<String> s = attrs.getKeys(u);
        Assert.assertEquals(s.size(), 2);
        Assert.assertTrue(s.containsAll(Arrays.asList("foo", "bar")));
        Assert.assertEquals(attrs.getKeys(2).size(), 0);
    }
}
