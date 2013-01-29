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
import btrplace.model.constraint.Ready;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SingleRunningCapacity;
import btrplace.plan.Action;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.ShutdownVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Unit tests for {@link CSingleRunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CSingleRunningCapacityTest extends ConstraintTestMaterial {

    @Test
    public void testDiscreteResolution() throws SolverException {

        Mapping map = new MappingBuilder().on(n1).run(n1, vm1, vm2).ready(vm3).get();
        Model mo = new DefaultModel(map);
        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(new Running(Collections.singleton(vm1)));
        l.add(new Ready(Collections.singleton(vm2)));
        l.add(new Running(Collections.singleton(vm3)));
        SingleRunningCapacity x = new SingleRunningCapacity(map.getAllNodes(), 2);
        x.setContinuous(false);
        l.add(x);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantDuration(10));
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertEquals(2, plan.getSize());
        Assert.assertEquals(SatConstraint.Sat.SATISFIED, x.isSatisfied(plan.getResult()));
    }

    @Test
    public void testContinuousResolution() throws SolverException {
        Mapping map = new MappingBuilder().on(n1).run(n1, vm1, vm2).ready(vm3).get();
        Model mo = new DefaultModel(map);
        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(new Running(Collections.singleton(vm1)));
        l.add(new Ready(Collections.singleton(vm2)));
        l.add(new Running(Collections.singleton(vm3)));
        SingleRunningCapacity sc = new SingleRunningCapacity(map.getAllNodes(), 2);
        sc.setContinuous(true);
        l.add(sc);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setTimeLimit(3);
        cra.labelVariables(true);
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantDuration(10));
        ReconfigurationPlan plan = cra.solve(mo, l);
        Assert.assertNotNull(plan);
        Iterator<Action> ite = plan.getActions().iterator();
        Assert.assertEquals(2, plan.getSize());
        Action a1 = ite.next();
        Action a2 = ite.next();
        Assert.assertTrue(a1 instanceof ShutdownVM);
        Assert.assertTrue(a2 instanceof BootVM);
        Assert.assertTrue(a1.getEnd() <= a2.getStart());
    }

    @Test
    public void testGetMisplaced() {
        Mapping m = new MappingBuilder().on(n1, n2).run(n1, vm1).ready(vm2, vm4).run(n2, vm3).get();
        Model mo = new DefaultModel(m);

        SingleRunningCapacity c = new SingleRunningCapacity(m.getAllNodes(), 1);
        CSingleRunningCapacity cc = new CSingleRunningCapacity(c);

        Assert.assertTrue(cc.getMisPlacedVMs(mo).isEmpty());
        m.addRunningVM(vm4, n2);
        Assert.assertEquals(m.getRunningVMs(n2), cc.getMisPlacedVMs(mo));
        m.addRunningVM(vm2, n1);
        Assert.assertEquals(m.getAllVMs(), cc.getMisPlacedVMs(mo));
    }
}
