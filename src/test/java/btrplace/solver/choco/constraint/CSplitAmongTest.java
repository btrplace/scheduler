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
import btrplace.model.constraint.SplitAmong;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CSplitAmong}.
 *
 * @author Fabien Hermenier
 */
public class CSplitAmongTest extends ConstraintTestMaterial {

    @Test
    public void testGetMisplaced() {
        Mapping map = new MappingBuilder().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7).get();

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Set<UUID> vg1 = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));
        Set<UUID> vg2 = new HashSet<UUID>(Arrays.asList(vm4, vm5, vm6));
        Set<UUID> vg3 = new HashSet<UUID>(Arrays.asList(vm7));

        Set<UUID> pg1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> pg2 = new HashSet<UUID>(Arrays.asList(n3, n4));
        Set<UUID> pg3 = new HashSet<UUID>(Arrays.asList(n5));
        Set<Set<UUID>> vgs = new HashSet<Set<UUID>>(Arrays.asList(vg1, vg2, vg3));
        Set<Set<UUID>> pgs = new HashSet<Set<UUID>>(Arrays.asList(pg1, pg2, pg3));

        SplitAmong s = new SplitAmong(vgs, pgs);
        CSplitAmong cs = new CSplitAmong(s);

        Model mo = new DefaultModel(map);

        Assert.assertTrue(cs.getMisPlacedVMs(mo).isEmpty());


        map.removeVM(vm7);
        map.addRunningVM(vm6, n5);
        //vg2 is on 2 group of nodes, the whole group is mis-placed

        Assert.assertEquals(cs.getMisPlacedVMs(mo), vg2);


        map.addRunningVM(vm7, n5);
        //vg1 and vg2 overlap on n1. The two groups are mis-placed
        map.addRunningVM(vm6, n2);

        Assert.assertTrue(cs.getMisPlacedVMs(mo).containsAll(vg1));
        Assert.assertTrue(cs.getMisPlacedVMs(mo).containsAll(vg2));
        Assert.assertEquals(cs.getMisPlacedVMs(mo).size(), vg1.size() + vg2.size());
    }

    @Test
    public void testDiscrete() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7).get();

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Set<UUID> vg1 = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));
        Set<UUID> vg2 = new HashSet<UUID>(Arrays.asList(vm4, vm5, vm6));
        Set<UUID> vg3 = new HashSet<UUID>(Arrays.asList(vm7));

        Set<UUID> pg1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> pg2 = new HashSet<UUID>(Arrays.asList(n3, n4));
        Set<UUID> pg3 = new HashSet<UUID>(Arrays.asList(n5));
        Set<Set<UUID>> vgs = new HashSet<Set<UUID>>(Arrays.asList(vg1, vg2, vg3));
        Set<Set<UUID>> pgs = new HashSet<Set<UUID>>(Arrays.asList(pg1, pg2, pg3));

        SplitAmong s = new SplitAmong(vgs, pgs);
        s.setContinuous(false);

        //vg1 and vg2 overlap on n2. The two groups are mis-placed
        map.addRunningVM(vm6, n2);

        Model mo = new DefaultModel(map);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>singleton(s));
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getSize() > 0);
    }

    @Test
    public void testContinuousWithAllDiffViolated() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7).get();

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Set<UUID> vg1 = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));
        Set<UUID> vg2 = new HashSet<UUID>(Arrays.asList(vm4, vm5, vm6));
        Set<UUID> vg3 = new HashSet<UUID>(Arrays.asList(vm7));

        Set<UUID> pg1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> pg2 = new HashSet<UUID>(Arrays.asList(n3, n4));
        Set<UUID> pg3 = new HashSet<UUID>(Arrays.asList(n5));
        Set<Set<UUID>> vgs = new HashSet<Set<UUID>>(Arrays.asList(vg1, vg2, vg3));
        Set<Set<UUID>> pgs = new HashSet<Set<UUID>>(Arrays.asList(pg1, pg2, pg3));

        SplitAmong s = new SplitAmong(vgs, pgs);
        s.setContinuous(true);

        //vg1 and vg2 overlap on n2.
        map.addRunningVM(vm6, n2);

        Model mo = new DefaultModel(map);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        Assert.assertNull(cra.solve(mo, Collections.<SatConstraint>singleton(s)));
    }

    @Test
    public void testContinuousWithGroupChange() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7).get();

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Set<UUID> vg1 = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));
        Set<UUID> vg2 = new HashSet<UUID>(Arrays.asList(vm4, vm5, vm6));

        Set<UUID> pg1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> pg2 = new HashSet<UUID>(Arrays.asList(n3, n4));
        Set<UUID> pg3 = new HashSet<UUID>(Arrays.asList(n5));
        Set<Set<UUID>> vgs = new HashSet<Set<UUID>>(Arrays.asList(vg1, vg2));
        Set<Set<UUID>> pgs = new HashSet<Set<UUID>>(Arrays.asList(pg1, pg2, pg3));

        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        SplitAmong s = new SplitAmong(vgs, pgs);
        s.setContinuous(true);

        //Move group of VMs 1 to the group of nodes 2. Cannot work as
        //the among part of the constraint will be violated
        Fence f = new Fence(vg1, pg2);

        Model mo = new DefaultModel(map);

        cstrs.add(s);
        cstrs.add(f);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        Assert.assertNull(cra.solve(mo, cstrs));
    }

    @Test
    public void testDiscreteWithGroupChange() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm3)
                .run(n2, vm2)
                .run(n3, vm4, vm6)
                .run(n4, vm5)
                .run(n5, vm7).get();

        //Isolated VM not considered by the constraint
        map.addRunningVM(vm8, n1);

        Set<UUID> vg1 = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));
        Set<UUID> vg2 = new HashSet<UUID>(Arrays.asList(vm4, vm5, vm6));

        Set<UUID> pg1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> pg2 = new HashSet<UUID>(Arrays.asList(n3, n4));
        Set<UUID> pg3 = new HashSet<UUID>(Arrays.asList(n5));
        Set<Set<UUID>> vgs = new HashSet<Set<UUID>>(Arrays.asList(vg1, vg2));
        Set<Set<UUID>> pgs = new HashSet<Set<UUID>>(Arrays.asList(pg1, pg2, pg3));

        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        SplitAmong s = new SplitAmong(vgs, pgs);
        s.setContinuous(false);

        //Move group of VMs 1 to the group of nodes 2. This is allowed
        //group of VMs 2 will move to another group of node so at the end, the constraint should be satisfied
        Fence f = new Fence(vg1, pg2);

        Model mo = new DefaultModel(map);

        cstrs.add(s);
        cstrs.add(f);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
    }

}
