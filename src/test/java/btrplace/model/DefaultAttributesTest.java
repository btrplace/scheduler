/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link DefaultAttributes}.
 *
 * @author Fabien Hermenier
 */
public class DefaultAttributesTest {

    @Test
    public void testInstantiation() {
        Attributes attrs = new DefaultAttributes();
        Assert.assertFalse(attrs.toString().contains("null"));
        Assert.assertTrue(attrs.get(UUID.randomUUID()).isEmpty());
        Assert.assertNull(attrs.get(UUID.randomUUID(), "foo"));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testSetAndGet() {
        Attributes attrs = new DefaultAttributes();
        UUID u = UUID.randomUUID();
        Assert.assertNull(attrs.set(u, "foo", "bar"));
        Assert.assertEquals("bar", attrs.get(u, "foo"));

        Assert.assertEquals("bar", attrs.set(u, "foo", "baz"));
        Assert.assertEquals("baz", attrs.get(u, "foo"));

        Assert.assertNull(attrs.set(u, "fi"));
        Assert.assertEquals(Boolean.TRUE, attrs.get(u, "fi"));
        Assert.assertEquals(Boolean.TRUE, attrs.set(u, "fi", Boolean.FALSE));
        Assert.assertEquals(Boolean.FALSE, attrs.get(u, "fi"));

        System.out.println(attrs);
    }

    @Test(dependsOnMethods = {"testSetAndGet", "testInstantiation"})
    public void testIsSet() {
        Attributes attrs = new DefaultAttributes();
        UUID e = UUID.randomUUID();
        Assert.assertFalse(attrs.isSet(e, "foo"));
        attrs.set(e, "foo");
        Assert.assertTrue(attrs.isSet(e, "foo"));
    }

    @Test(dependsOnMethods = {"testSetAndGet", "testInstantiation"})
    public void testUnset() {
        Attributes attrs = new DefaultAttributes();
        UUID e = UUID.randomUUID();
        Assert.assertNull(attrs.unset(e, "foo"));
        attrs.set(e, "foo");
        Assert.assertEquals(Boolean.TRUE, attrs.unset(e, "foo"));
        Assert.assertFalse(attrs.isSet(e, "foo"));
        Assert.assertNull(attrs.unset(e, "foo"));
    }

    @Test(dependsOnMethods = {"testSetAndGet", "testInstantiation"})
    public void testGetAsSet() {
        Attributes attrs = new DefaultAttributes();
        UUID e = UUID.randomUUID();
        Assert.assertTrue(attrs.get(e).isEmpty());
        attrs.set(e, "foo");
        Set<String> res = new HashSet<String>();
        res.add("foo");
        Assert.assertEquals(res, attrs.get(e));
        res.add("bar");
        attrs.set(e, "bar", 1);
        Assert.assertEquals(res, attrs.get(e));
    }

    @Test(dependsOnMethods = {"testSetAndGet", "testInstantiation", "testUnset"})
    public void testClone() {
        Attributes attrs = new DefaultAttributes();
        List<UUID> l = new ArrayList<UUID>();
        for (int i = 0; i < 5; i++) {
            UUID u = UUID.randomUUID();
            attrs.set(u, Integer.toString(i), i);
            l.add(u);
        }
        Attributes attrs2 = attrs.clone();
        for (UUID u : l) {
            Assert.assertEquals(attrs.get(u), attrs2.get(u));
        }

        UUID f = UUID.randomUUID();
        Assert.assertEquals(attrs.get(f), attrs2.get(f));

        attrs.unset(l.get(0), "0");
        Assert.assertEquals(0, attrs2.get(l.get(0), "0"));

        attrs2.unset(l.get(1), "1");
        Assert.assertEquals(1, attrs.get(l.get(1), "1"));
    }

    @Test(dependsOnMethods = {"testSetAndGet", "testInstantiation", "testUnset", "testClone"})
    public void testEqualsHashCode() {
        Attributes attrs = new DefaultAttributes();
        List<UUID> l = new ArrayList<UUID>();
        for (int i = 0; i < 5; i++) {
            UUID u = UUID.randomUUID();
            attrs.set(u, Integer.toString(i), i);
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

    @Test(dependsOnMethods = {"testInstantiation", "testSetAndGet"})
    public void testClear() {
        Attributes attrs = new DefaultAttributes();
        List<UUID> l = new ArrayList<UUID>();
        for (int i = 0; i < 5; i++) {
            UUID u = UUID.randomUUID();
            attrs.set(u, Integer.toString(i), i);
            l.add(u);
        }
        attrs.clear();
        for (UUID u : l) {
            Assert.assertTrue(attrs.get(u).isEmpty());
        }
    }
}
