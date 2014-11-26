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

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.*;
import org.btrplace.model.constraint.Among;
import org.btrplace.model.constraint.Fence;
import org.btrplace.model.constraint.Running;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CAmong}.
 *
 * @author Fabien Hermenier
 */
public class CAmongTest {

    @Test
    public void testWithOnGroup() throws SchedulerException {

        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping())
                .on(n1, n2, n3, n4)
                .run(n1, vm1).run(n2, vm2).run(n3, vm3)
                .ready(vm4, vm5).get();

        Set<VM> vms = new HashSet<>(Arrays.asList(vm1, vm2, vm3));

        Collection<Collection<Node>> pGrps = new HashSet<>();
        Set<Node> s = new HashSet<>();
        s.add(n1);
        s.add(n2);
        pGrps.add(s);
        Among a = new Among(vms, pGrps);
        a.setContinuous(false);
        ChocoScheduler cra = new DefaultChocoScheduler();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Running.newRunning(map.getAllVMs()));
        cstrs.add(a);

        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        //System.out.println(p);
        //Assert.assertEquals(a.isSatisfied(p.getResult()), SatConstraint.Sat.SATISFIED);
    }

    @Test
    public void testWithGroupChange() throws SchedulerException {

        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping())
                .on(n1, n2, n3, n4)
                .run(n1, vm1).run(n2, vm2, vm3)
                .ready(vm4, vm5).get();

        Set<VM> vms = new HashSet<>(Arrays.asList(vm1, vm2, vm5));

        Collection<Node> s1 = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> s2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Collection<Node>> pGrps = Arrays.asList(s1, s2);
        Among a = new Among(vms, pGrps);
        a.setContinuous(false);
        ChocoScheduler cra = new DefaultChocoScheduler();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Running.newRunning(map.getAllVMs()));
        cstrs.add(new Fence(vm2, s2));
        cstrs.add(a);

        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        //System.out.println(p);
        //Assert.assertEquals(a.isSatisfied(p.getResult()), SatConstraint.Sat.SATISFIED);
    }

    /**
     * No solution because constraints force to spread the VMs among 2 groups.
     *
     * @throws org.btrplace.scheduler.SchedulerException
     */
    @Test
    public void testWithNoSolution() throws SchedulerException {

        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping())
                .on(n1, n2, n3, n4)
                .run(n1, vm1).run(n2, vm2, vm3)
                .ready(vm4, vm5).get();

        Set<VM> vms = new HashSet<>(Arrays.asList(vm1, vm2, vm5));


        Collection<Node> s = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> s2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Collection<Node>> pGrps = new HashSet<>(Arrays.asList(s, s2));

        Among a = new Among(vms, pGrps);
        a.setContinuous(false);
        ChocoScheduler cra = new DefaultChocoScheduler();
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Running.newRunning(map.getAllVMs()));
        cstrs.add(new Fence(vm2, Collections.singleton(n3)));
        cstrs.add(new Fence(vm1, Collections.singleton(n1)));
        cstrs.add(a);

        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNull(p);
    }

    @Test
    public void testGetMisplaced() {

        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping())
                .on(n1, n2, n3, n4)
                .run(n1, vm1).run(n2, vm2, vm3)
                .ready(vm4, vm5).get();

        Set<VM> vms = new HashSet<>(Arrays.asList(vm1, vm2, vm5));
        Collection<Node> s1 = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> s2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Collection<Node>> pGrps = new HashSet<>(Arrays.asList(s1, s2));

        Among a = new Among(vms, pGrps);
        CAmong ca = new CAmong(a);
        Assert.assertEquals(ca.getMisPlacedVMs(mo), Collections.emptySet());

        map.addRunningVM(vm5, n3);
        Assert.assertEquals(ca.getMisPlacedVMs(mo), vms);
    }

    @Test
    public void testContinuousWithAlreadySatisfied() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping())
                .on(n1, n2, n3, n4)
                .run(n1, vm1).run(n2, vm2, vm3)
                .ready(vm4, vm5).get();

        Set<VM> vms = new HashSet<>(Arrays.asList(vm1, vm2, vm5));
        Collection<Node> s1 = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> s2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Collection<Node>> pGrps = new HashSet<>(Arrays.asList(s1, s2));

        Among a = new Among(vms, pGrps);
        a.setContinuous(true);

        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Running.newRunning(map.getAllVMs()));
        cstrs.add(a);

        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
    }

    @Test
    public void testContinuousWithNotAlreadySatisfied() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping())
                .on(n1, n2, n3, n4)
                .run(n1, vm1).run(n2, vm2).run(n3, vm3)
                .ready(vm4, vm5).get();

        Set<VM> vms = new HashSet<>(Arrays.asList(vm1, vm2, vm5));

        Collection<Node> s1 = new HashSet<>(Arrays.asList(n1, n2));
        Collection<Node> s2 = new HashSet<>(Arrays.asList(n3, n4));
        Collection<Collection<Node>> pGrps = new HashSet<>(Arrays.asList(s1, s2));

        Among a = new Among(vms, pGrps);
        a.setContinuous(true);

        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Running.newRunning(map.getAllVMs()));
        cstrs.add(new Fence(vm2, Collections.singleton(n3)));
        cstrs.add(a);

        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNull(p);
    }

    /*
    @Test
    public void testContinuousWithOneRunningAndChange() throws SchedulerException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        Mapping map = new MappingFiller(mo.getMapping())
                .on(n1, n2)
                .run(n1, vm1).ready(vm2, vm3).get();

        Set<VM> vms = new HashSet<>(Arrays.asList(vm1, vm2, vm3));

        Collection<Node> s1 = new HashSet<>(Arrays.asList(n1));
        Collection<Node> s2 = new HashSet<>(Arrays.asList(n2));
        Collection<Collection<Node>> pGrps = new HashSet<>(Arrays.asList(s1, s2));

        Among a = new Among(vms, pGrps);
        a.setContinuous(true);

        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Running.newRunning(map.getAllVMs()));
        cstrs.add(new Fence(vm1, Collections.singleton(n2)));
        cstrs.add(a);

        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.doRepair(true);
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
    }            */
}
