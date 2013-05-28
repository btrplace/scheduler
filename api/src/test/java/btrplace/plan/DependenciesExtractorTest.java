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

package btrplace.plan;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.view.ShareableResource;
import btrplace.plan.event.*;
import btrplace.test.PremadeElements;
import junit.framework.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link DependenciesExtractor}.
 *
 * @author Fabien Hermenier
 */
public class DependenciesExtractorTest implements PremadeElements {

    MigrateVM m1 = new MigrateVM(vm1, n2, n1, 0, 5);
    MigrateVM m2 = new MigrateVM(vm2, n3, n4, 0, 5);
    BootNode b1 = new BootNode(n5, 0, 5);
    BootVM r1 = new BootVM(vm3, n5, 5, 7);
    ShutdownNode s1 = new ShutdownNode(n6, 3, 7);
    MigrateVM m3 = new MigrateVM(vm4, n6, n2, 0, 2);
    MigrateVM m4 = new MigrateVM(vm5, n6, n2, 7, 9);

    /**
     * Disjoint reconfiguration graph, so no dependencies
     */
    @Test
    public void testDisjointGraphs() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOnlineNode(n4);
        map.addOnlineNode(n6);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n3);
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
        map.addOnlineNode(n5);
        map.addReadyVM(vm3);
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
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n6);
        map.addRunningVM(vm1, n2);
        map.addRunningVM(vm4, n6);
        DependenciesExtractor ex = new DependenciesExtractor(mo);
        Assert.assertTrue(ex.visit(m1));
        Assert.assertTrue(ex.visit(m3));
        Assert.assertTrue(ex.visit(m4));

        Assert.assertTrue(ex.getDependencies(m1).toString(), ex.getDependencies(m1).isEmpty());
        Assert.assertTrue(ex.getDependencies(m3).toString(), ex.getDependencies(m3).isEmpty());
        Assert.assertEquals(ex.getDependencies(m4).toString(), ex.getDependencies(m4).size(), 1);
        Assert.assertTrue(ex.getDependencies(m4).toString(), ex.getDependencies(m4).contains(m1));
    }

    @Test
    public void testDependenciesWithShutdown() {
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(n2);
        map.addOnlineNode(n6);
        map.addRunningVM(vm4, n6);
        DependenciesExtractor ex = new DependenciesExtractor(mo);
        Assert.assertTrue(ex.visit(s1));
        Assert.assertTrue(ex.visit(m3));

        Assert.assertTrue(ex.getDependencies(m3).toString(), ex.getDependencies(m3).isEmpty());
        Assert.assertEquals(ex.getDependencies(s1).toString(), ex.getDependencies(s1).size(), 1);
        Assert.assertTrue(ex.getDependencies(s1).toString(), ex.getDependencies(s1).contains(m3));
    }

    @Test
    public void testDependencyWithAllocate() {

        //An increase allocation is impossible until a decreasing allocation
        Model mo = new DefaultModel();
        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);

        ShareableResource rc = new ShareableResource("cpu", 0, 0);
        rc.setVMConsumption(vm1, 3);
        rc.setVMConsumption(vm2, 5);

        mo.attach(rc);

        DependenciesExtractor ex = new DependenciesExtractor(mo);
        Allocate a1 = new Allocate(vm1, n1, "cpu", 5, 5, 7); // 3->5
        Allocate a2 = new Allocate(vm2, n1, "cpu", 3, 0, 3); // 5->3
        Assert.assertTrue(ex.visit(a1));
        Assert.assertTrue(ex.visit(a2));
        Assert.assertTrue(ex.getDependencies(a2).toString(), ex.getDependencies(a2).isEmpty());
        Assert.assertEquals(ex.getDependencies(a1).toString(), ex.getDependencies(a1).size(), 1);
        Assert.assertTrue(ex.getDependencies(a1).toString(), ex.getDependencies(a1).contains(a2));
    }

}
