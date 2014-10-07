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

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.BootNode;
import org.btrplace.plan.event.ShutdownNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MaxOnlineTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();

        Set<Node> s = new HashSet<>(Arrays.asList(mo.newNode(), mo.newNode()));
        MaxOnline o = new MaxOnline(s, 3);
        Assert.assertNotNull(o.getChecker());
        Assert.assertEquals(o.getInvolvedNodes(), s);
        Assert.assertTrue(o.getInvolvedVMs().isEmpty());
        Assert.assertNotNull(o.toString());
        Assert.assertFalse(o.isContinuous());
        Assert.assertTrue(o.setContinuous(true));
        Assert.assertTrue(o.isContinuous());
        Assert.assertTrue(o.setContinuous(false));
        Assert.assertTrue(o.equals(new MaxOnline(s, 3)));
        Assert.assertEquals(o.hashCode(), new MaxOnline(s, 3, false).hashCode());
        Assert.assertFalse(o.equals(new MaxOnline(s, 4)));
        Assert.assertFalse(o.equals(new MaxOnline(Collections.singleton(mo.newNode()), 3)));
        Assert.assertFalse(o.equals(new MaxOnline(s, 3, true)));
    }

    public void testEquals() {
        Model m = new DefaultModel();
        Set<Node> s = new HashSet<>(Arrays.asList(m.newNode(), m.newNode()));
        MaxOnline mo = new MaxOnline(s, 3);
        Assert.assertEquals(mo, new MaxOnline(s, 3));
        Assert.assertNotEquals(mo, new MaxOnline(s, 1));
        Assert.assertNotEquals(mo, new MaxOnline(new HashSet<Node>(), 3));
        Assert.assertNotEquals(new MaxOnline(s, 3, true), new MaxOnline(s, 3, false));
        Assert.assertEquals(mo, new MaxOnline(s, 3));
        Assert.assertNotEquals(new MaxOnline(s, 3, true).hashCode(), new MaxOnline(s, 3, false).hashCode());
        Assert.assertEquals(new MaxOnline(s, 3, true), new MaxOnline(s, 3, false));
    }

    @Test
    public void isSatisfiedModel() {
        Model model = new DefaultModel();
        Mapping map = model.getMapping();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        Set<Node> s = new HashSet<Node>(Arrays.asList(n1, n2, n3));
        MaxOnline mo = new MaxOnline(s, 2);

        Assert.assertTrue(mo.isSatisfied(model));

        model.getMapping().addOnlineNode(n3);
        Assert.assertFalse(mo.isSatisfied(model));
    }

    @Test
    public void isSatisfiedReconfigurationPlan() {
        Model model = new DefaultModel();
        Mapping map = model.getMapping();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOfflineNode(n3);

        Set<Node> s = new HashSet<Node>(Arrays.asList(n1, n2, n3));
        MaxOnline mo = new MaxOnline(s, 2);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(model);

        Assert.assertTrue(mo.isSatisfied(plan));

        plan.add(new BootNode(n3, 3, 9));
        Assert.assertFalse(mo.isSatisfied(plan));

        plan.add(new ShutdownNode(n2, 0, 5));
        Assert.assertTrue(mo.isSatisfied(plan));

    }
}
