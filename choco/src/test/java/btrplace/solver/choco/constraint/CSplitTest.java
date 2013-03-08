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
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.Split;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CSplit}.
 *
 * @author Fabien Hermenier
 */
public class CSplitTest implements PremadeElements {

    private static Set<UUID> g1 = new HashSet<UUID>(Arrays.asList(vm1, vm2));
    private static Set<UUID> g2 = new HashSet<UUID>(Arrays.asList(vm3, vm4, vm5));
    private static Set<UUID> g3 = new HashSet<UUID>(Arrays.asList(vm6, vm7));

    private static Set<Set<UUID>> grps = new HashSet<Set<UUID>>(Arrays.asList(g1, g2, g3));

    @Test
    public void testGetMisplaced() {

        Set<Set<UUID>> grps = new HashSet<Set<UUID>>(Arrays.asList(g1, g2, g3));

        Mapping map = new MappingBuilder().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm2)
                .run(n2, vm3)
                .run(n3, vm4, vm5)
                .run(n4, vm6)
                .run(n5, vm7, vm8).build();

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

        Mapping map = new MappingBuilder().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm2, vm3/* violation*/)
                .run(n3, vm4, vm5, vm6/*violation*/)
                .run(n5, vm7, vm8).build();

        Split s = new Split(grps);

        Model mo = new DefaultModel(map);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>singleton(s));
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getSize() > 0);
        //System.out.println(p);
    }

    @Test
    public void testContinuous() throws SolverException {

        Mapping map = new MappingBuilder().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm2)
                .run(n3, vm3, vm4, vm5)
                .run(n5, vm6, vm7, vm8).build();

        Split s = new Split(grps);
        s.setContinuous(true);
        Model mo = new DefaultModel(map);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(s);
        //What is running on n1 goes to n3, so VMs vm3, vm4, vm5 which does not belong to vm1, vm2 must
        //go away before the other arrive.
        cstrs.add(new Fence(map.getRunningVMs(n1), Collections.singleton(n3)));
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getSize() > 0);
    }
}
