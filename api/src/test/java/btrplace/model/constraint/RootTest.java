/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

package btrplace.model.constraint;

import btrplace.model.*;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.MigrateVM;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link btrplace.model.constraint.Root}.
 *
 * @author Fabien Hermenier
 */
public class RootTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<VM> x = new HashSet<>(Util.newVMs(new DefaultModel(), 2));
        Root s = new Root(x);
        Assert.assertNotNull(s.getChecker());
        Assert.assertEquals(x, s.getInvolvedVMs());
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
        Set<VM> x = new HashSet<>(Util.newVMs(new DefaultModel(), 2));
        Root s = new Root(x);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Root(x).equals(s));
        Assert.assertEquals(s.hashCode(), new Root(x).hashCode());
        x = new HashSet<>(Util.newVMs(new DefaultModel(), 1));
        Assert.assertFalse(new Root(x).equals(s));
    }

    @Test
    public void testIsSatisfied() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 2);
        Mapping map = mo.getMapping();
        map.addReadyVM(vms.get(0));
        map.addReadyVM(vms.get(1));
        Root o = new Root(new HashSet<>(vms));

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
        Root r = new Root(Collections.singleton(vm1));
        Assert.assertEquals(r.isSatisfied(p), true);
        p.add(new MigrateVM(vm1, ns.get(0), ns.get(1), 1, 2));
        Assert.assertEquals(r.isSatisfied(p), false);
    }

}
