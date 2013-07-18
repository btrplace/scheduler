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

package btrplace.solver.choco.runner.staticPartitioning.splitter;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link Splitters}.
 *
 * @author Fabien Hermenier
 */
public class SplittersTest {

    @Test
    public void testExtractInside() {
        Set<Integer> s = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        Set<Integer> in = new HashSet<>(Arrays.asList(3, 5, 8, 10));
        Set<Integer> removed = Splitters.extractInside(s, in);
        Assert.assertEquals(removed, new HashSet<>(Arrays.asList(3, 5)));
        Assert.assertEquals(s.size(), 3);
        Assert.assertFalse(s.contains(3));
        Assert.assertFalse(s.contains(5));

        removed = Splitters.extractInside(s, in);
        Assert.assertEquals(removed.size(), 0);
    }

    @Test
    public void testExtractNodesIn() {
        Model mo = new DefaultModel();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();
        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addOnlineNode(n4);
        mo.getMapping().addOfflineNode(n2);
        List<Node> root = new ArrayList<>(Arrays.asList(n1, n2, n3));
        Set<Node> res = Splitters.extractNodesIn(root, mo.getMapping());
        Assert.assertTrue(res.containsAll(Arrays.asList(n1, n2)));
        Assert.assertEquals(mo.getMapping().getAllNodes().size(), 3);
        Assert.assertEquals(root.size(), 1);
        Assert.assertTrue(root.contains(n3));
    }

    @Test
    public void testExtractVMsIn() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        Node n1 = mo.newNode();
        mo.getMapping().addOnlineNode(n1);
        mo.getMapping().addReadyVM(vm1);
        mo.getMapping().addRunningVM(vm2, n1);
        mo.getMapping().addSleepingVM(vm3, n1);
        mo.getMapping().addSleepingVM(mo.newVM(), n1);
        List<VM> root = new ArrayList<>(Arrays.asList(vm1, vm2, vm3, vm4));
        Set<VM> res = Splitters.extractVMsIn(root, mo.getMapping());
        Assert.assertTrue(res.containsAll(Arrays.asList(vm1, vm2, vm3)));
        Assert.assertEquals(mo.getMapping().getAllVMs().size(), 4);
        Assert.assertEquals(root.size(), 1);
        Assert.assertTrue(root.contains(vm4));

    }
}
