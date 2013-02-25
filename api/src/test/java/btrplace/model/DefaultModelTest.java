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

import java.util.UUID;

/**
 * Unit tests for {@link btrplace.model.DefaultModel}.
 *
 * @author Fabien Hermenier
 */
public class DefaultModelTest {

    class MockView implements ModelView {

        private String i;

        public MockView(String id) {
            i = id;
        }

        @Override
        public String getIdentifier() {
            return i;
        }

        @Override
        public MockView clone() {
            return new MockView(i);
        }

        @Override
        public int hashCode() {
            return i.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            ModelView v = (ModelView) o;
            return v.getIdentifier().equals(this.getIdentifier());
        }
    }

    @Test
    public void testInstantiate() {
        Mapping c = new DefaultMapping();
        Model i = new DefaultModel(c);
        Assert.assertEquals(i.getMapping(), c);
        Assert.assertTrue(i.getViews().isEmpty());
        Assert.assertNotNull(i.getAttributes());
    }

    @Test
    public void testAttachView() {
        Model i = new DefaultModel(new DefaultMapping());
        ModelView v = new MockView("mock");

        Assert.assertTrue(i.attach(v));
        Assert.assertEquals(i.getViews().size(), 1);
        Assert.assertEquals(i.getView("mock"), v);
        Assert.assertEquals(i.getView("bar"), null);

        Assert.assertFalse(i.attach(v));
        Assert.assertEquals(i.getViews().size(), 1);
        Assert.assertEquals(i.getView("mock"), v);

        ModelView v2 = new MockView("bar");

        Assert.assertTrue(i.attach(v2));
        Assert.assertEquals(i.getViews().size(), 2);
        Assert.assertEquals(i.getView("bar"), v2);
    }


    @Test(dependsOnMethods = {"testAttachView", "testInstantiate"})
    public void testEqualsAndHashCode() {
        Model i = new DefaultModel(new DefaultMapping());
        ModelView rc = new MockView("foo");
        ModelView b = new MockView("bar");
        i.attach(rc);
        i.attach(b);

        UUID u = UUID.randomUUID();
        i.getAttributes().put(u, "foo", true);
        Model j = new DefaultModel(i.getMapping().clone());
        j.getAttributes().put(u, "foo", true);
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

    @Test(dependsOnMethods = {"testInstantiate", "testEqualsAndHashCode", "testAttachView", "testDetachView", "testAttributes"})
    public void testClone() {
        Model i = new DefaultModel(new DefaultMapping());
        ModelView v1 = new MockView("foo");
        ModelView v2 = new MockView("bar");
        UUID u = UUID.randomUUID();
        i.getAttributes().put(u, "foo", false);
        i.attach(v1);
        i.attach(v2);
        Model c = i.clone();
        Assert.assertEquals(c.hashCode(), i.hashCode());
        Assert.assertTrue(c.equals(i));
        i.detach(v1);
        Assert.assertEquals(c.getView("foo"), v1);
        c.detach(v1);
        Assert.assertEquals(i.getView("bar"), v2);
        Assert.assertEquals(c.getAttributes().getBoolean(u, "foo"), Boolean.FALSE);

    }

    @Test(dependsOnMethods = {"testAttachView", "testInstantiate"})
    public void testDetachView() {
        Model i = new DefaultModel(new DefaultMapping());
        ModelView v = new MockView("cpu");
        i.attach(v);
        Assert.assertTrue(i.detach(v));
        Assert.assertTrue(i.getViews().isEmpty());
        Assert.assertNull(i.getView("cpu"));
        Assert.assertFalse(i.detach(v));
    }

    @Test(dependsOnMethods = {"testAttachView", "testInstantiate"})
    public void testClearViews() {
        Model i = new DefaultModel(new DefaultMapping());
        i.attach(new MockView("cpu"));
        i.attach(new MockView("mem"));
        i.clearViews();
        Assert.assertTrue(i.getViews().isEmpty());
    }

    @Test
    public void testAttributes() {
        Model i = new DefaultModel(new DefaultMapping());
        Attributes attrs = new DefaultAttributes();
        attrs.put(UUID.randomUUID(), "foo", true);
        i.setAttributes(attrs);
        Assert.assertEquals(i.getAttributes(), attrs);
    }
}
