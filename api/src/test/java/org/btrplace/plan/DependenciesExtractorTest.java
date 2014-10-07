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

package org.btrplace.plan;

import org.btrplace.model.*;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.event.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Unit tests for {@link DependenciesExtractor}.
 *
 * @author Fabien Hermenier
 */
public class DependenciesExtractorTest {

    List<VM> vms = Util.newVMs(10);
    List<Node> ns = Util.newNodes(10);

    MigrateVM m1 = new MigrateVM(vms.get(0), ns.get(1), ns.get(0), 0, 5);
    MigrateVM m2 = new MigrateVM(vms.get(1), ns.get(2), ns.get(3), 0, 5);
    BootNode b1 = new BootNode(ns.get(4), 0, 5);
    BootVM r1 = new BootVM(vms.get(2), ns.get(4), 5, 7);
    ShutdownNode s1 = new ShutdownNode(ns.get(5), 3, 7);
    MigrateVM m3 = new MigrateVM(vms.get(3), ns.get(5), ns.get(1), 0, 2);
    MigrateVM m4 = new MigrateVM(vms.get(4), ns.get(5), ns.get(1), 7, 9);

    /**
     * Disjoint reconfiguration graph, so no dependencies
     */
    @Test
    public void testDisjointGraphs() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(2));
        map.addOnlineNode(ns.get(3));
        map.addOnlineNode(ns.get(5));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(2));
        DependenciesExtractor ex = new DependenciesExtractor(mo);
        Assert.assertTrue(ex.visit(m1));
        Assert.assertTrue(ex.visit(m2));
        Assert.assertTrue(ex.visit(s1));
        Assert.assertTrue(ex.getDependencies(m1).isEmpty());
        Assert.assertTrue(ex.getDependencies(m2).isEmpty());
        Assert.assertTrue(ex.getDependencies(s1).isEmpty());
    }

    @Test
    public void testSimpleDependencies() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(4));
        map.addReadyVM(vms.get(2));
        DependenciesExtractor ex = new DependenciesExtractor(mo);
        Assert.assertTrue(ex.visit(b1));
        Assert.assertTrue(ex.visit(r1));
        Assert.assertTrue(ex.getDependencies(b1).isEmpty());
        Assert.assertEquals(ex.getDependencies(r1).size(), 1);
        Assert.assertTrue(ex.getDependencies(r1).contains(b1));
    }

    @Test
    public void testNoDependencyDueToTiming() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(5));
        map.addRunningVM(vms.get(0), ns.get(1));
        map.addRunningVM(vms.get(3), ns.get(5));
        DependenciesExtractor ex = new DependenciesExtractor(mo);
        Assert.assertTrue(ex.visit(m1));
        Assert.assertTrue(ex.visit(m3));
        Assert.assertTrue(ex.visit(m4));

        Assert.assertTrue(ex.getDependencies(m1).isEmpty(), ex.getDependencies(m1).toString());
        Assert.assertTrue(ex.getDependencies(m3).isEmpty(), ex.getDependencies(m3).toString());
        Assert.assertEquals(ex.getDependencies(m4).size(), 1, ex.getDependencies(m4).toString());
        Assert.assertTrue(ex.getDependencies(m4).contains(m1), ex.getDependencies(m4).toString());
    }

    @Test
    public void testDependenciesWithShutdown() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(1));
        map.addOnlineNode(ns.get(5));
        map.addRunningVM(vms.get(3), ns.get(5));
        DependenciesExtractor ex = new DependenciesExtractor(mo);
        Assert.assertTrue(ex.visit(s1));
        Assert.assertTrue(ex.visit(m3));

        Assert.assertTrue(ex.getDependencies(m3).isEmpty(), ex.getDependencies(m3).toString());
        Assert.assertEquals(ex.getDependencies(s1).size(), 1, ex.getDependencies(s1).toString());
        Assert.assertTrue(ex.getDependencies(s1).contains(m3), ex.getDependencies(s1).toString());
    }

    @Test
    public void testDependencyWithAllocate() {

        //An increase allocation is impossible until a decreasing allocation
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(ns.get(0));
        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(0));

        ShareableResource rc = new ShareableResource("cpu", 0, 0);
        rc.setConsumption(vms.get(0), 3);
        rc.setConsumption(vms.get(1), 5);

        mo.attach(rc);

        DependenciesExtractor ex = new DependenciesExtractor(mo);
        Allocate a1 = new Allocate(vms.get(0), ns.get(0), "cpu", 5, 5, 7); // 3->5
        Allocate a2 = new Allocate(vms.get(1), ns.get(0), "cpu", 3, 0, 3); // 5->3
        Assert.assertTrue(ex.visit(a1));
        Assert.assertTrue(ex.visit(a2));
        Assert.assertTrue(ex.getDependencies(a2).isEmpty(), ex.getDependencies(a2).toString());
        Assert.assertEquals(ex.getDependencies(a1).size(), 1, ex.getDependencies(a1).toString());
        Assert.assertTrue(ex.getDependencies(a1).contains(a2), ex.getDependencies(a1).toString());
    }

}
