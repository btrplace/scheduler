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

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link Fence}.
 *
 * @author Fabien Hermenier
 */
public class FenceTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<Integer> vms = new HashSet<>(Arrays.asList(vm1));
        Set<Integer> nodes = new HashSet<>(Arrays.asList(n1));
        Fence f = new Fence(vms, nodes);
        Assert.assertNotNull(f.getChecker());
        Assert.assertEquals(vms, f.getInvolvedVMs());
        Assert.assertEquals(nodes, f.getInvolvedNodes());
        Assert.assertFalse(f.toString().contains("null"));
        Assert.assertFalse(f.isContinuous());
        Assert.assertFalse(f.setContinuous(true));
        System.out.println(f);
    }

    @Test
    public void testIsSatisfied() {
        Model m = new DefaultModel();
        Mapping map = m.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n2);
        Set<Integer> vms = new HashSet<>(Arrays.asList(vm1, vm2, vm3));

        Set<Integer> nodes = new HashSet<>(Arrays.asList(n1, n2));

        Fence f = new Fence(vms, nodes);
        Assert.assertEquals(true, f.isSatisfied(m));
        map.addRunningVM(vm3, n3);
        Assert.assertEquals(false, f.isSatisfied(m));
    }

    @Test
    public void testEquals() {
        Set<Integer> vms = new HashSet<>(Arrays.asList(vm1, vm2));
        Set<Integer> nodes = new HashSet<>(Arrays.asList(n1, n2));
        Fence f = new Fence(vms, nodes);
        Assert.assertTrue(f.equals(f));
        Assert.assertTrue(new Fence(vms, nodes).equals(f));
        Assert.assertEquals(new Fence(vms, nodes).hashCode(), f.hashCode());

        Assert.assertFalse(new Fence(nodes, vms).equals(f));
    }
}
