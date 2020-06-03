/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

import org.btrplace.model.view.ModelView;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(b.copy()).thenReturn(b);
        when(rc.copy()).thenReturn(rc);
        i.attach(rc);
        i.attach(b);

        VM vm = i.newVM();
        i.getAttributes().put(vm, "foo", true);
        Model j = i.copy();
        j.getAttributes().put(vm, "foo", true);
        j.attach(rc);
        j.attach(b);
        Assert.assertTrue(i.equals(i));
        Assert.assertEquals(i.hashCode(), j.hashCode());
        j.detach(rc);
        Assert.assertFalse(i.equals(j));
        j.attach(rc);
        j.getMapping().addReadyVM(j.newVM());
        Assert.assertFalse(i.equals(j));
        j.getAttributes().put(vm, "bar", false);
        Assert.assertFalse(i.equals(j));
        j.setAttributes(i.getAttributes());
    }

    @Test(dependsOnMethods = {"testInstantiate", "testEqualsAndHashCode", "testAttachView", "testDetachView", "testAttributes"})
    public void testClone() {
        Model i = new DefaultModel();
        ModelView v1 = mock(ModelView.class);
        when(v1.getIdentifier()).thenReturn("foo");
        when(v1.copy()).thenReturn(v1);
        ModelView v2 = mock(ModelView.class);
        when(v2.getIdentifier()).thenReturn("bar");
        when(v2.copy()).thenReturn(v2);
        VM u = i.newVM();
        i.getAttributes().put(u, "foo", false);
        i.attach(v1);
        i.attach(v2);
        Model c = i.copy();
        Assert.assertEquals(c.hashCode(), i.hashCode());
        Assert.assertTrue(c.equals(i));
        i.detach(v1);
        Assert.assertEquals(c.getView("foo"), v1);
        c.detach(v1);
        Assert.assertEquals(i.getView("bar"), v2);
        Assert.assertEquals(c.getAttributes().get(u, "foo"), Boolean.FALSE);

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
