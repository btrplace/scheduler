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

        int nextId = v.id() + 1000;
        VM v2 = eb.newVM(nextId);
        Assert.assertTrue(eb.contains(v2));
    }

    @Test
    public void testNodeRegistration() {
        ElementBuilder eb = new DefaultElementBuilder();
        Node n = eb.newNode();
        Assert.assertTrue(eb.contains(n));
        Assert.assertNull(eb.newNode(n.id()));

        int nextId = n.id() + 1000;
        Node n2 = eb.newNode(nextId);
        Assert.assertTrue(eb.contains(n2));
    }
}
