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
import btrplace.model.constraint.SequentialVMTransitions;
import btrplace.model.constraint.Sleeping;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link CSequentialVMTransitions}.
 *
 * @author Fabien Hermenier
 */
public class CSequentialVMTransitionsTest {

    UUID vm1 = UUID.randomUUID();
    UUID vm2 = UUID.randomUUID();
    UUID vm3 = UUID.randomUUID();
    UUID vm4 = UUID.randomUUID();

    UUID n1 = UUID.randomUUID();
    UUID n2 = UUID.randomUUID();

    @Test
    public void testWithOnlyTransitions() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addReadyVM(vm1);
        map.addRunningVM(vm2, n1);
        map.addSleepingVM(vm3, n2);
        map.addRunningVM(vm4, n1);
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        Model mo = new DefaultModel(map);
        cstrs.add(new Running(Collections.singleton(vm1)));
        cstrs.add(new Sleeping(Collections.singleton(vm2)));
        cstrs.add(new Running(Collections.singleton(vm3)));
        cstrs.add(new Ready(Collections.singleton(vm4)));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        List<UUID> seq = new ArrayList<UUID>();
        seq.add(vm1);
        seq.add(vm2);
        seq.add(vm3);
        seq.add(vm4);
        cstrs.add(new SequentialVMTransitions(seq));
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
//        System.out.println(plan);
    }

    @Test
    public void testWithVMsWithNoTransitions() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addReadyVM(vm1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n1);
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        Model mo = new DefaultModel(map);
        cstrs.add(new Running(Collections.singleton(vm1)));
        cstrs.add(new Sleeping(Collections.singleton(vm2)));
        cstrs.add(new Running(Collections.singleton(vm3)));
        cstrs.add(new Ready(Collections.singleton(vm4)));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        List<UUID> seq = new ArrayList<UUID>();
        seq.add(vm1);
        seq.add(vm2);
        seq.add(vm3);
        seq.add(vm4);
        cstrs.add(new SequentialVMTransitions(seq));
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
        System.out.println(plan);
    }
}
