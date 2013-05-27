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

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.MigrateVM;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link Ban}.
 *
 * @author Fabien Hermenier
 */
public class BanTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<Integer> vms = new HashSet<>(Arrays.asList(vm1));
        Set<Integer> nodes = new HashSet<>(Arrays.asList(n1));
        Ban b = new Ban(vms, nodes);
        Assert.assertEquals(vms, b.getInvolvedVMs());
        Assert.assertEquals(nodes, b.getInvolvedNodes());
        Assert.assertFalse(b.toString().contains("null"));
        Assert.assertFalse(b.isContinuous());
        Assert.assertFalse(b.setContinuous(true));
        Assert.assertNotNull(b.getChecker());
        System.out.println(b);
    }

    @Test
    public void testIsSatisfied() {

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n3);
        Set<Integer> vms = new HashSet<>(Arrays.asList(vm2, vm3));

        Set<Integer> nodes = new HashSet<>(Arrays.asList(n1));

        Ban b = new Ban(vms, nodes);
        Model m = new DefaultModel(map);
        Assert.assertEquals(b.isSatisfied(m), true);
        map.addRunningVM(vm3, n1);
        Assert.assertEquals(b.isSatisfied(m), false);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(m);
        plan.add(new MigrateVM(vm1, n1, n2, 0, 3));
        plan.add(new MigrateVM(vm1, n2, n1, 3, 6));
        plan.add(new MigrateVM(vm2, n2, n3, 3, 6));
        Assert.assertEquals(b.isSatisfied(plan), false);
    }

    @Test
    public void testEquals() {
        Set<Integer> vms = new HashSet<>(Arrays.asList(vm1, vm2));
        Set<Integer> nodes = new HashSet<>(Arrays.asList(n1, n2));

        Ban b = new Ban(vms, nodes);
        Assert.assertTrue(b.equals(b));
        Assert.assertTrue(new Ban(vms, nodes).equals(b));
        Assert.assertEquals(new Ban(vms, nodes).hashCode(), b.hashCode());

        Assert.assertFalse(new Ban(nodes, vms).equals(b));
    }
}
