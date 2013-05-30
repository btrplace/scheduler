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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link Fence}.
 *
 * @author Fabien Hermenier
 */
public class FenceTest {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);

        Set<VM> vs = new HashSet<>(Arrays.asList(vms.get(0)));
        Set<Node> nodes = new HashSet<>(Arrays.asList(ns.get(0)));
        Fence f = new Fence(vs, nodes);
        Assert.assertNotNull(f.getChecker());
        Assert.assertEquals(vs, f.getInvolvedVMs());
        Assert.assertEquals(nodes, f.getInvolvedNodes());
        Assert.assertFalse(f.toString().contains("null"));
        Assert.assertFalse(f.isContinuous());
        Assert.assertFalse(f.setContinuous(true));
        System.out.println(f);
    }

    @Test
    public void testIsSatisfied() {
        Model m = new DefaultModel();
        List<Node> ns = Util.newNodes(m, 10);
        List<VM> vms = Util.newVMs(m, 10);

        Mapping map = m.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(1));
        map.addRunningVM(vms.get(2), ns.get(1));
        Set<VM> vs = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1), vms.get(2)));

        Set<Node> nodes = new HashSet<>(Arrays.asList(ns.get(0), ns.get(1)));

        Fence f = new Fence(vs, nodes);
        Assert.assertEquals(true, f.isSatisfied(m));
        map.addRunningVM(vms.get(2), ns.get(2));
        Assert.assertEquals(false, f.isSatisfied(m));
    }

    @Test
    public void testEquals() {

        Model mo = new DefaultModel();
        Set<VM> vms = new HashSet<>(Arrays.asList(mo.newVM(), mo.newVM()));
        Set<Node> nodes = new HashSet<>(Arrays.asList(mo.newNode(), mo.newNode()));
        Fence f = new Fence(vms, nodes);
        Assert.assertTrue(f.equals(f));
        Assert.assertTrue(new Fence(vms, nodes).equals(f));
        Assert.assertEquals(new Fence(vms, nodes).hashCode(), f.hashCode());
    }
}
