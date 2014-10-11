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
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.MigrateVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Unit tests for {@link org.btrplace.model.constraint.Root}.
 *
 * @author Fabien Hermenier
 */
public class RootTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();

        Root s = new Root(v);
        Assert.assertNotNull(s.getChecker());
        Assert.assertEquals(s.getInvolvedVMs().iterator().next(), v);
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertNotNull(s.toString());
        System.out.println(s);
        Assert.assertTrue(s.isContinuous());
        Assert.assertFalse(s.setContinuous(false));
        Assert.assertTrue(s.isContinuous());
        Assert.assertTrue(s.setContinuous(true));
        Assert.assertTrue(s.isContinuous());
    }

    @Test
    public void testEquals() {
        Model mo = new DefaultModel();
        VM v = mo.newVM();

        Root s = new Root(v);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Root(v).equals(s));
        Assert.assertEquals(s.hashCode(), new Root(v).hashCode());
        Assert.assertFalse(new Root(mo.newVM()).equals(s));
    }

    @Test
    public void testIsSatisfied() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        VM v = mo.newVM();
        map.addReadyVM(v);
        Root o = new Root(v);

        Assert.assertEquals(o.isSatisfied(mo), true);
        map.clear();
        Assert.assertEquals(o.isSatisfied(mo), true);
    }

    @Test
    public void testContinuousIsSatisfied() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        List<Node> ns = Util.newNodes(mo, 3);
        VM vm1 = mo.newVM();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addRunningVM(vm1, ns.get(0));
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        Root r = new Root(vm1);
        Assert.assertEquals(r.isSatisfied(p), true);
        p.add(new MigrateVM(vm1, ns.get(0), ns.get(1), 1, 2));
        Assert.assertEquals(r.isSatisfied(p), false);
    }

}
