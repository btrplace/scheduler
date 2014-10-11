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

import org.btrplace.model.view.ModelView;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link org.btrplace.model.DefaultModel}.
 *
 * @author Fabien Hermenier
 */
public class DefaultModelTest {


    @Test
    public void testInstantiate() {
        Model i = new DefaultModel();
        Assert.assertTrue(i.getViews().isEmpty());
        Assert.assertNotNull(i.getAttributes());
    }

    @Test
    public void testAttachView() {
        Model i = new DefaultModel();
        ModelView v = mock(ModelView.class);
        when(v.getIdentifier()).thenReturn("mock");
        Assert.assertTrue(i.attach(v));
        Assert.assertEquals(i.getViews().size(), 1);
        Assert.assertEquals(i.getView("mock"), v);
        Assert.assertEquals(i.getView("bar"), null);

        Assert.assertFalse(i.attach(v));
        Assert.assertEquals(i.getViews().size(), 1);
        Assert.assertEquals(i.getView("mock"), v);

        ModelView v2 = mock(ModelView.class);
        when(v2.getIdentifier()).thenReturn("bar");

        Assert.assertTrue(i.attach(v2));
        Assert.assertEquals(i.getViews().size(), 2);
        Assert.assertEquals(i.getView("bar"), v2);
    }


    @Test(dependsOnMethods = {"testAttachView", "testInstantiate"})
    public void testEqualsAndHashCode() {
        Model i = new DefaultModel();
        ModelView rc = mock(ModelView.class);
        when(rc.getIdentifier()).thenReturn("foo");
        ModelView b = mock(ModelView.class);
        when(b.getIdentifier()).thenReturn("bar");
        when(b.clone()).thenReturn(b);
        when(rc.clone()).thenReturn(rc);
        i.attach(rc);
        i.attach(b);

        VM vm = i.newVM();
        i.getAttributes().put(vm, "foo", true);
        Model j = i.clone();
        j.getAttributes().put(vm, "foo", true);
        j.attach(rc);
        j.attach(b);
        Assert.assertTrue(i.equals(i));
        Assert.assertTrue(i.equals(j));
        Assert.assertEquals(i.hashCode(), j.hashCode());
        j.detach(rc);
        Assert.assertFalse(i.equals(j));
        j.attach(rc);
        j.getMapping().addReadyVM(j.newVM());
        Assert.assertFalse(i.equals(j));
    }

    @Test(dependsOnMethods = {"testInstantiate", "testEqualsAndHashCode", "testAttachView", "testDetachView", "testAttributes"})
    public void testClone() {
        Model i = new DefaultModel();
        ModelView v1 = mock(ModelView.class);
        when(v1.getIdentifier()).thenReturn("foo");
        when(v1.clone()).thenReturn(v1);
        ModelView v2 = mock(ModelView.class);
        when(v2.getIdentifier()).thenReturn("bar");
        when(v2.clone()).thenReturn(v2);
        VM u = i.newVM();
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
        Model i = new DefaultModel();
        ModelView v = mock(ModelView.class);
        when(v.getIdentifier()).thenReturn("cpu");
        i.attach(v);
        Assert.assertTrue(i.detach(v));
        Assert.assertTrue(i.getViews().isEmpty());
        Assert.assertNull(i.getView("cpu"));
        Assert.assertFalse(i.detach(v));
    }

    @Test(dependsOnMethods = {"testAttachView", "testInstantiate"})
    public void testClearViews() {
        Model i = new DefaultModel();
        ModelView v1 = mock(ModelView.class);
        when(v1.getIdentifier()).thenReturn("cpu");

        ModelView v2 = mock(ModelView.class);
        when(v2.getIdentifier()).thenReturn("mem");

        i.attach(v1);
        i.attach(v2);
        i.clearViews();
        Assert.assertTrue(i.getViews().isEmpty());
    }

    @Test
    public void testAttributes() {
        Model i = new DefaultModel();
        Attributes attrs = new DefaultAttributes();
        attrs.put(i.newVM(), "foo", true);
        i.setAttributes(attrs);
        Assert.assertEquals(i.getAttributes(), attrs);
    }

    @Test
    public void testElementCreation() {
        ElementBuilder eb = mock(ElementBuilder.class);
        Model mo = new DefaultModel(eb);
        mo.newVM();
        verify(eb).newVM();
        mo.newVM(5);
        verify(eb).newVM(5);

        mo.newNode();
        verify(eb).newNode();
        mo.newNode(5);
        verify(eb).newNode(5);

        mo.contains(new VM(1));
        verify(eb).contains(new VM(1));

        mo.contains(new Node(7));
        verify(eb).contains(new Node(7));

    }
    /*@Test
    public void testBig() {
        int nbNodes = 1000000;
        int ratio = 10;

        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        for (int i = 0; i < nbNodes; i++) {
            Node n = mo.newNode();
            map.addOnlineNode(n);
            for (int j = 0; j < ratio; j++) {
                VM v = mo.newVM();
                map.addRunningVM(v, n);
            }
        }
        for (int i = 0; i < ratio; i++) {
            VM v = mo.newVM();
            map.addReadyVM(v);
        }
    }       */
}
