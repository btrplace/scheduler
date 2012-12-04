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
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.action.ShutdownVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.durationEvaluator.ConstantDuration;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link CSingleRunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CSingleRunningCapacityTest {

    @Test
    public void testContinuous() throws SolverException {
        ChocoLogging.setVerbosity(Verbosity.FINEST);
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
        l.add(new Running(Collections.singleton(vm3)));
        l.add(new Ready(Collections.singleton(vm2)));
        l.add(new SingleRunningCapacity(map.getAllNodes(), 2));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        cra.getDurationEvaluators().register(ShutdownVM.class, new ConstantDuration(10));
        ReconfigurationPlan plan = cra.solve(mo, l);
        System.out.println(plan);
        Assert.fail();
    }
}
