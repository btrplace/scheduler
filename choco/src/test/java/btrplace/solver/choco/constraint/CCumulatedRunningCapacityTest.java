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
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.ShutdownVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link btrplace.solver.choco.constraint.CCumulatedRunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CCumulatedRunningCapacityTest implements PremadeElements {

    @Test
    public void testWithSatisfiedConstraint() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n3, vm3, vm4)
                .sleep(n2, vm5).build();
        Model mo = new DefaultModel(map);
        List<SatConstraint> l = new ArrayList<>();
        CumulatedRunningCapacity x = new CumulatedRunningCapacity(map.getAllNodes(), 4);
        x.setContinuous(false);
        l.add(x);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantDuration(10));
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertEquals(plan.getSize(), 0);
    }

    @Test
    public void testDiscreteSatisfaction() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4, vm5).build();
        Set<UUID> on = new HashSet<>(Arrays.asList(n1, n2));
        Model mo = new DefaultModel(map);
        List<SatConstraint> l = new ArrayList<>();
        CumulatedRunningCapacity x = new CumulatedRunningCapacity(on, 4);
        x.setContinuous(false);
        l.add(x);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantDuration(10));
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        //System.out.println(plan);
        Assert.assertEquals(plan.getSize(), 1);
    }


    @Test
    public void testFeasibleContinuousResolution() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4).ready(vm5).build();
        Set<UUID> on = new HashSet<>(Arrays.asList(n1, n2));
        Model mo = new DefaultModel(map);
        List<SatConstraint> l = new ArrayList<>();
        l.add(new Running(Collections.singleton(vm5)));
        l.add(new Fence(Collections.singleton(vm5), Collections.singleton(n1)));
        CumulatedRunningCapacity x = new CumulatedRunningCapacity(on, 4);
        x.setContinuous(true);
        l.add(x);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        for (SatConstraint c : l) {
            System.out.println(c);
        }
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantDuration(10));
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        //System.out.println(plan);
        Assert.assertEquals(plan.getSize(), 2);
    }

    @Test
    public void testUnFeasibleContinuousResolution() throws SolverException {
        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .ready(vm1)
                .run(n1, vm2)
                .run(n2, vm3, vm4)
                .run(n3, vm5).build();

        Model mo = new DefaultModel(map);
        List<SatConstraint> l = new ArrayList<>();

        List<UUID> seq = new ArrayList<>();
        seq.add(vm1);
        seq.add(vm2);
        l.add(new SequentialVMTransitions(seq));
        l.add(new Fence(Collections.singleton(vm1), Collections.singleton(n1)));
        l.add(new Sleeping(Collections.singleton(vm2)));
        l.add(new Running(Collections.singleton(vm1)));
        l.add(new Root(Collections.singleton(vm3)));
        l.add(new Root(Collections.singleton(vm4)));

        Set<UUID> on = new HashSet<>(Arrays.asList(n1, n2));
        CumulatedRunningCapacity x = new CumulatedRunningCapacity(on, 3);
        x.setContinuous(true);
        l.add(x);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setMaxEnd(5);
        cra.labelVariables(true);
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantDuration(10));
        ReconfigurationPlan plan = cra.solve(mo, l);
        System.out.println(plan);
        Assert.assertNull(plan);
    }

    @Test
    public void testGetMisplaced() {
        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm2, vm3)
                .run(n2, vm4).ready(vm5).build();

        Model mo = new DefaultModel(map);

        CumulatedRunningCapacity c = new CumulatedRunningCapacity(map.getAllNodes(), 4);
        CCumulatedRunningCapacity cc = new CCumulatedRunningCapacity(c);

        Assert.assertTrue(cc.getMisPlacedVMs(mo).isEmpty());
        map.addRunningVM(vm5, n3);
        Assert.assertEquals(cc.getMisPlacedVMs(mo), map.getAllVMs());
    }

    @Test
    public void testUnfeasible() throws SolverException {

        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n1, vm1, vm2)
                .run(n2, vm3)
                .run(n3, vm4).build();

        Model model = new DefaultModel(map);
        Collection<SatConstraint> ctrs = new HashSet<>();
        ctrs.add(new CumulatedRunningCapacity(map.getAllNodes(), 2));

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan plan = cra.solve(model, ctrs);
        Assert.assertNull(plan);

    }
}
