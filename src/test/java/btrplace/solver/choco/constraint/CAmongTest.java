/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace.solver.choco.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.Among;
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.Running;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CAmong}.
 *
 * @author Fabien Hermenier
 */
public class CAmongTest extends ConstraintTestMaterial {

    @Test
    public void testWithOnGroup() throws SolverException {

        Mapping map = new MappingBuilder()
                .on(n1, n2, n3, n4)
                .run(n1, vm1).run(n2, vm2).run(n3, vm3)
                .ready(vm4, vm5).get();

        Set<UUID> vms = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));

        Set<Set<UUID>> pGrps = new HashSet<Set<UUID>>();
        Set<UUID> s = new HashSet<UUID>();
        s.add(n1);
        s.add(n2);
        pGrps.add(s);
        Among a = new Among(vms, pGrps);
        a.setContinuous(false);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(new Running(map.getAllVMs()));
        cstrs.add(a);

        Model mo = new DefaultModel(map);
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(a.isSatisfied(p.getResult()), SatConstraint.Sat.SATISFIED);
    }

    @Test
    public void testWithGroupChange() throws SolverException {

        Mapping map = new MappingBuilder()
                .on(n1, n2, n3, n4)
                .run(n1, vm1).run(n2, vm2, vm3)
                .ready(vm4, vm5).get();

        Set<UUID> vms = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm5));

        Set<UUID> s1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> s2 = new HashSet<UUID>(Arrays.asList(n3, n4));
        Set<Set<UUID>> pGrps = new HashSet<Set<UUID>>(Arrays.asList(s1, s2));
        Among a = new Among(vms, pGrps);
        a.setContinuous(false);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(new Running(map.getAllVMs()));
        cstrs.add(new Fence(Collections.singleton(vm2), s2));
        cstrs.add(a);

        Model mo = new DefaultModel(map);
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(a.isSatisfied(p.getResult()), SatConstraint.Sat.SATISFIED);
    }

    /**
     * No solution because constraints force to spread the VMs among 2 groups.
     *
     * @throws SolverException
     */
    @Test
    public void testWithNoSolution() throws SolverException {

        Mapping map = new MappingBuilder()
                .on(n1, n2, n3, n4)
                .run(n1, vm1).run(n2, vm2, vm3)
                .ready(vm4, vm5).get();

        Set<UUID> vms = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm5));


        Set<UUID> s = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> s2 = new HashSet<UUID>(Arrays.asList(n3, n4));
        Set<Set<UUID>> pGrps = new HashSet<Set<UUID>>(Arrays.asList(s, s2));

        Among a = new Among(vms, pGrps);
        a.setContinuous(false);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(new Running(map.getAllVMs()));
        cstrs.add(new Fence(Collections.singleton(vm2), Collections.singleton(n3)));
        cstrs.add(new Fence(Collections.singleton(vm1), Collections.singleton(n1)));
        cstrs.add(a);

        Model mo = new DefaultModel(map);
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNull(p);
    }

    @Test
    public void testGetMisplaced() {

        Mapping map = new MappingBuilder()
                .on(n1, n2, n3, n4)
                .run(n1, vm1).run(n2, vm2, vm3)
                .ready(vm4, vm5).get();

        Set<UUID> vms = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm5));
        Set<UUID> s1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> s2 = new HashSet<UUID>(Arrays.asList(n3, n4));
        Set<Set<UUID>> pGrps = new HashSet<Set<UUID>>(Arrays.asList(s1, s2));

        Model mo = new DefaultModel(map);
        Among a = new Among(vms, pGrps);
        CAmong ca = new CAmong(a);
        Assert.assertEquals(ca.getMisPlacedVMs(mo), Collections.emptySet());

        map.addRunningVM(vm5, n3);
        Assert.assertEquals(ca.getMisPlacedVMs(mo), vms);
    }

    @Test
    public void testContinuousWithAlreadySatisfied() throws SolverException {
        Mapping map = new MappingBuilder()
                .on(n1, n2, n3, n4)
                .run(n1, vm1).run(n2, vm2, vm3)
                .ready(vm4, vm5).get();

        Set<UUID> vms = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm5));
        Set<UUID> s1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> s2 = new HashSet<UUID>(Arrays.asList(n3, n4));
        Set<Set<UUID>> pGrps = new HashSet<Set<UUID>>(Arrays.asList(s1, s2));

        Model mo = new DefaultModel(map);
        Among a = new Among(vms, pGrps);
        a.setContinuous(true);

        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(new Running(map.getAllVMs()));
        cstrs.add(a);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertEquals(a.isSatisfied(p.getResult()), SatConstraint.Sat.SATISFIED);
    }

    @Test
    public void testContinuousWithNotAlreadySatisfied() throws SolverException {
        Mapping map = new MappingBuilder()
                .on(n1, n2, n3, n4)
                .run(n1, vm1).run(n2, vm2).run(n3, vm3)
                .ready(vm4, vm5).get();

        Set<UUID> vms = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm5));

        Set<UUID> s1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> s2 = new HashSet<UUID>(Arrays.asList(n3, n4));
        Set<Set<UUID>> pGrps = new HashSet<Set<UUID>>(Arrays.asList(s1, s2));

        Model mo = new DefaultModel(map);
        Among a = new Among(vms, pGrps);
        a.setContinuous(true);

        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(new Running(map.getAllVMs()));
        cstrs.add(new Fence(Collections.singleton(vm2), Collections.singleton(n3)));
        cstrs.add(a);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNull(p);
    }
}
