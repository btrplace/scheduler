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

package org.btrplace.model.constraint;

import org.btrplace.model.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Unit tests for {@link org.btrplace.model.constraint.Online}.
 *
 * @author Fabien Hermenier
 */
public class OnlineTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        Node n = mo.newNode();
        Online o = new Online(n);
        Assert.assertNotNull(o.getChecker());
        Assert.assertTrue(o.getInvolvedNodes().contains(n));
        Assert.assertTrue(o.getInvolvedVMs().isEmpty());
        Assert.assertNotNull(o.toString());
        Assert.assertTrue(o.setContinuous(true));
        System.out.println(o);
    }

    @Test
    public void testIsSatisfied() {
        Model i = new DefaultModel();
        List<Node> ns = Util.newNodes(i, 3);

        Mapping c = i.getMapping();
        c.addOnlineNode(ns.get(0));
        Online o = new Online(ns.get(0));
        Assert.assertEquals(o.isSatisfied(i), true);
        c.addOfflineNode(ns.get(1));
        Assert.assertEquals(new Online(ns.get(1)).isSatisfied(i), false);
    }

    @Test
    public void testEquals() {
        Model mo = new DefaultModel();
        Node n = mo.newNode();
        Online s = new Online(n);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Online(n).equals(s));
        Assert.assertEquals(new Online(n).hashCode(), s.hashCode());
        Assert.assertFalse(new Online(mo.newNode()).equals(s));
    }
}
