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
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.Split;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CSplit}.
 *
 * @author Fabien Hermenier
 */
public class CSplitTest extends ConstraintTestMaterial {

    @Test
    public void testGetMisplaced() {
        Set<UUID> g1 = new HashSet<UUID>();
        Set<UUID> g2 = new HashSet<UUID>();
        Set<UUID> g3 = new HashSet<UUID>();

        g1.add(vm1);
        g1.add(vm2);

        g2.add(vm3);
        g2.add(vm4);
        g2.add(vm5);

        g3.add(vm6);
        g3.add(vm7);

        Set<Set<UUID>> grps = new HashSet<Set<UUID>>();
        grps.add(g1);
        grps.add(g2);
        grps.add(g3);

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOnlineNode(n4);
        map.addOnlineNode(n5);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm8, n1);

        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n3);
        map.addRunningVM(vm5, n3);

        map.addRunningVM(vm6, n4);
        map.addRunningVM(vm7, n5);
        map.addRunningVM(vm8, n5);

        Split s = new Split(grps);
        CSplit cs = new CSplit(s);

        Model mo = new DefaultModel(map);
        Assert.assertTrue(cs.getMisPlacedVMs(mo).isEmpty());

        map.addRunningVM(vm5, n1);
        Set<UUID> bad = cs.getMisPlacedVMs(mo);
        Assert.assertEquals(bad.size(), 3);

        Assert.assertTrue(bad.contains(vm1) && bad.contains(vm2) && bad.contains(vm5));
        map.addRunningVM(vm6, n3);
        bad = cs.getMisPlacedVMs(mo);
        Assert.assertTrue(bad.contains(vm4) && bad.contains(vm5) && bad.contains(vm6));

    }

    @Test
    public void testSimpleDiscrete() throws SolverException {
        Set<UUID> g1 = new HashSet<UUID>();
        Set<UUID> g2 = new HashSet<UUID>();
        Set<UUID> g3 = new HashSet<UUID>();

        g1.add(vm1);
        g1.add(vm2);

        g2.add(vm3);
        g2.add(vm4);
        g2.add(vm5);

        g3.add(vm6);
        g3.add(vm7);

        Set<Set<UUID>> grps = new HashSet<Set<UUID>>();
        grps.add(g1);
        grps.add(g2);
        grps.add(g3);

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOnlineNode(n4);
        map.addOnlineNode(n5);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm8, n1);

        map.addRunningVM(vm3, n1); //Violation
        map.addRunningVM(vm4, n3);
        map.addRunningVM(vm5, n3);

        map.addRunningVM(vm6, n3); //Violation
        map.addRunningVM(vm7, n5);
        map.addRunningVM(vm8, n5);

        Split s = new Split(grps);

        Model mo = new DefaultModel(map);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();

        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>singleton(s));
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getSize() > 0);
        System.out.println(p);
    }

    @Test
    public void testContinuous() throws SolverException {
        Set<UUID> g1 = new HashSet<UUID>();
        Set<UUID> g2 = new HashSet<UUID>();
        Set<UUID> g3 = new HashSet<UUID>();

        g1.add(vm1);
        g1.add(vm2);

        g2.add(vm3);
        g2.add(vm4);
        g2.add(vm5);

        g3.add(vm6);
        g3.add(vm7);

        Set<Set<UUID>> grps = new HashSet<Set<UUID>>();
        grps.add(g1);
        grps.add(g2);
        grps.add(g3);

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOnlineNode(n4);
        map.addOnlineNode(n5);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm8, n1);

        map.addRunningVM(vm3, n3);
        map.addRunningVM(vm4, n3);
        map.addRunningVM(vm5, n3);

        map.addRunningVM(vm6, n5);
        map.addRunningVM(vm7, n5);
        map.addRunningVM(vm8, n5);

        Split s = new Split(grps);
        s.setContinuous(true);
        Model mo = new DefaultModel(map);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();

        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(s);
        cstrs.add(new Fence(map.getRunningVMs(n1), Collections.singleton(n3)));
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getSize() > 0);
        System.out.println(p);
    }
}
