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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link btrplace.model.DefaultModel}.
 *
 * @author Fabien Hermenier
 */
public class DefaultModelTest {

    @Test
    public void testInstantiate() {
        Mapping c = new DefaultMapping();
        Model i = new DefaultModel(c);
        Assert.assertEquals(c, i.getMapping());
        Assert.assertTrue(i.getResources().isEmpty());

        Set<ShareableResource> rcs = new HashSet<ShareableResource>();
        rcs.add(new DefaultShareableResource("foo"));
        rcs.add(new DefaultShareableResource("bar"));
    }

    @Test
    public void testAttachResource() {
        Model i = new DefaultModel(new DefaultMapping());
        ShareableResource rc = new DefaultShareableResource("foo");
        Assert.assertTrue(i.attach(rc));
        Assert.assertEquals(1, i.getResources().size());
        Assert.assertEquals(rc, i.getResource("foo"));
        Assert.assertEquals(null, i.getResource("bar"));

        Assert.assertFalse(i.attach(rc));
        Assert.assertEquals(1, i.getResources().size());
        Assert.assertEquals(rc, i.getResource("foo"));

        ShareableResource b = new DefaultShareableResource("bar");
        Assert.assertTrue(i.attach(b));
        Assert.assertEquals(2, i.getResources().size());
        Assert.assertEquals(b, i.getResource("bar"));
    }


    @Test(dependsOnMethods = {"testAttachResource", "testInstantiate"})
    public void testEqualsAndHashCode() {
        Model i = new DefaultModel(new DefaultMapping());
        ShareableResource rc = new DefaultShareableResource("foo");
        ShareableResource b = new DefaultShareableResource("bar");
        i.attach(rc);
        i.attach(b);

        UUID u = UUID.randomUUID();
        i.getAttributes().set(u, "foo");
        Model j = new DefaultModel(i.getMapping().clone());
        j.getAttributes().set(u, "foo");
        j.attach(rc);
        j.attach(b);
        Assert.assertTrue(i.equals(i));
        Assert.assertTrue(i.equals(j));
        Assert.assertEquals(i.hashCode(), j.hashCode());
        j.detach(rc);
        Assert.assertFalse(i.equals(j));
        j.attach(rc);
        j.getMapping().addReadyVM(UUID.randomUUID());
        Assert.assertFalse(i.equals(j));
    }

    @Test(dependsOnMethods = {"testInstantiate", "testEqualsAndHashCode", "testAttachResource", "testDetachStackableResource"})
    public void testClone() {
        Model i = new DefaultModel(new DefaultMapping());
        ShareableResource rc = new DefaultShareableResource("foo");
        ShareableResource b = new DefaultShareableResource("bar");
        UUID u = UUID.randomUUID();
        i.getAttributes().set(u, "foo");
        i.attach(rc);
        i.attach(b);
        Model c = i.clone();
        Assert.assertTrue(c.equals(i));
        Assert.assertEquals(c.hashCode(), i.hashCode());
        i.detach(rc);
        Assert.assertEquals(rc, c.getResource("foo"));
        c.detach(b);
        Assert.assertEquals(b, i.getResource("bar"));
        Assert.assertEquals(Boolean.TRUE, c.getAttributes().get(u, "foo"));

    }

    @Test(dependsOnMethods = {"testAttachResource", "testInstantiate"})
    public void testDetachStackableResource() {
        Model i = new DefaultModel(new DefaultMapping());
        ShareableResource rc = new DefaultShareableResource("cpu");
        i.attach(rc);
        Assert.assertTrue(i.detach(rc));
        Assert.assertTrue(i.getResources().isEmpty());
        Assert.assertNull(i.getResource("cpu"));
        Assert.assertFalse(i.detach(rc));
    }

    @Test(dependsOnMethods = {"testAttachResource", "testInstantiate"})
    public void testClearResource() {
        Model i = new DefaultModel(new DefaultMapping());
        i.attach(new DefaultShareableResource("cpu"));
        i.attach(new DefaultShareableResource("mem"));
        i.clearResources();
        Assert.assertTrue(i.getResources().isEmpty());
    }

    public void testAttributes() {

    }
}
