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
import btrplace.model.constraint.Ready;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SingleRunningCapacity;
import btrplace.plan.Action;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.action.BootVM;
import btrplace.plan.action.ShutdownVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CSingleRunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CSingleRunningCapacityTest {


    @Test
    public void testDiscrete() throws SolverException {
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        UUID n1 = UUID.randomUUID();
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addReadyVM(vm3);
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
        Iterator<Action> ite = plan.getActions().iterator();
        Assert.assertEquals(2, plan.getSize());
        Assert.assertEquals(SatConstraint.Sat.SATISFIED, x.isSatisfied(plan.getResult()));
    }

    @Test
    public void testContinuous() throws SolverException {
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        UUID n1 = UUID.randomUUID();
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addReadyVM(vm3);
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
        System.out.println(plan);
        Iterator<Action> ite = plan.getActions().iterator();
        Assert.assertEquals(2, plan.getSize());
        Action a1 = ite.next();
        Action a2 = ite.next();
        Assert.assertTrue(a1 instanceof ShutdownVM);
        Assert.assertTrue(a2 instanceof BootVM);
        Assert.assertTrue(a1.getEnd() <= a2.getStart());
    }
}
