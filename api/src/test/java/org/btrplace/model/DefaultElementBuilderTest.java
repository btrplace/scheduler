/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link DefaultElementBuilder}.
 *
 * @author Fabien Hermenier
 */
public class DefaultElementBuilderTest {

    @Test
    public void testVMRegistration() {
        ElementBuilder eb = new DefaultElementBuilder();
        VM v = eb.newVM();
        VM vX = eb.newVM();
        Assert.assertNotEquals(v, vX);
        Assert.assertTrue(eb.contains(v));
        Assert.assertTrue(eb.contains(vX));
        Assert.assertNull(eb.newVM(v.id()));

        int nextId = vX.id() + 1;
        VM v2 = eb.newVM(nextId);
        Assert.assertTrue(eb.contains(v2));

        VM v3 = eb.newVM();
        Assert.assertNotEquals(v3, v2);
    }

    @Test
    public void testNodeRegistration() {
        ElementBuilder eb = new DefaultElementBuilder();
        Node n = eb.newNode();
        Assert.assertTrue(eb.contains(n));
        Assert.assertNull(eb.newNode(n.id()));

        int nextId = n.id() + 1;
        Node n2 = eb.newNode(nextId);
        Assert.assertTrue(eb.contains(n2));

        // Reproduce issue #156
        Node n3 = eb.newNode();
        Assert.assertNotEquals(n2, n3);
    }

}
