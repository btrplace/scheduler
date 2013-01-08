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

import btrplace.model.*;
import btrplace.model.constraint.Among;
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.Running;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CAmong}.
 *
 * @author Fabien Hermenier
 */
public class CAmongTest {

    UUID vm1 = UUID.randomUUID();
    UUID vm2 = UUID.randomUUID();
    UUID vm3 = UUID.randomUUID();
    UUID vm4 = UUID.randomUUID();
    UUID vm5 = UUID.randomUUID();

    UUID n1 = UUID.randomUUID();
    UUID n2 = UUID.randomUUID();
    UUID n3 = UUID.randomUUID();
    UUID n4 = UUID.randomUUID();

    @Test
    public void testWithOnGroup() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOnlineNode(n4);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n3);
        map.addReadyVM(vm4);
        map.addReadyVM(vm5);

        Set<UUID> vms = new HashSet<UUID>();
        vms.add(vm1);
        vms.add(vm2);
        vms.add(vm3);

        Set<Set<UUID>> pGrps = new HashSet<Set<UUID>>();
        Set<UUID> s = new HashSet<UUID>();
        s.add(n1);
        s.add(n2);
        pGrps.add(s);
        Among a = new Among(vms, pGrps);
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
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOnlineNode(n4);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n2);
        map.addReadyVM(vm4);
        map.addReadyVM(vm5);

        Set<UUID> vms = new HashSet<UUID>();
        vms.add(vm1);
        vms.add(vm2);
        vms.add(vm5);

        Set<Set<UUID>> pGrps = new HashSet<Set<UUID>>();
        Set<UUID> s = new HashSet<UUID>();
        s.add(n1);
        s.add(n2);
        pGrps.add(s);

        s = new HashSet<UUID>();
        s.add(n3);
        s.add(n4);
        pGrps.add(s);
        Among a = new Among(vms, pGrps);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(new Running(map.getAllVMs()));
        cstrs.add(new Fence(Collections.singleton(vm2), Collections.singleton(n3)));
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
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOnlineNode(n4);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n2);
        map.addReadyVM(vm4);
        map.addReadyVM(vm5);

        Set<UUID> vms = new HashSet<UUID>();
        vms.add(vm1);
        vms.add(vm2);
        vms.add(vm5);

        Set<Set<UUID>> pGrps = new HashSet<Set<UUID>>();
        Set<UUID> s = new HashSet<UUID>();
        s.add(n1);
        s.add(n2);
        pGrps.add(s);

        s = new HashSet<UUID>();
        s.add(n3);
        s.add(n4);
        pGrps.add(s);
        Among a = new Among(vms, pGrps);
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
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOnlineNode(n4);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n2);
        map.addReadyVM(vm4);
        map.addReadyVM(vm5);

        Set<UUID> vms = new HashSet<UUID>();
        vms.add(vm1);
        vms.add(vm2);
        vms.add(vm5);

        Set<Set<UUID>> pGrps = new HashSet<Set<UUID>>();
        Set<UUID> s = new HashSet<UUID>();
        s.add(n1);
        s.add(n2);
        pGrps.add(s);

        Model mo = new DefaultModel(map);

        s = new HashSet<UUID>();
        s.add(n3);
        s.add(n4);
        pGrps.add(s);
        Among a = new Among(vms, pGrps);
        CAmong ca = new CAmong(a);
        Assert.assertEquals(ca.getMisPlacedVMs(mo), Collections.emptySet());

        map.addRunningVM(vm5, n3);
        Assert.assertEquals(ca.getMisPlacedVMs(mo), vms);
    }
}
