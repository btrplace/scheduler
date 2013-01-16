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
import btrplace.model.constraint.Lonely;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link CLonely}.
 *
 * @author Fabien Hermenier
 */
public class CLonelyTest {

    private UUID vm1 = UUID.randomUUID();
    private UUID vm2 = UUID.randomUUID();
    private UUID vm3 = UUID.randomUUID();
    private UUID vm4 = UUID.randomUUID();
    private UUID vm5 = UUID.randomUUID();

    private UUID n1 = UUID.randomUUID();
    private UUID n2 = UUID.randomUUID();
    private UUID n3 = UUID.randomUUID();

    @Test
    public void testFeasibleDiscrete() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n2);
        map.addRunningVM(vm5, n2);

        Set<UUID> mine = new HashSet<UUID>();
        mine.add(vm1);
        mine.add(vm2);
        mine.add(vm3);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        Model mo = new DefaultModel(map);
        Lonely l = new Lonely(mine);
        l.setContinuous(false);
        ReconfigurationPlan plan = cra.solve(mo, Collections.<SatConstraint>singleton(l));
        Assert.assertNotNull(plan);
        System.out.println(plan);
        Assert.assertEquals(l.isSatisfied(plan.getResult()), SatConstraint.Sat.SATISFIED);
    }

    /**
     * vm3 needs to go to n2 . vm1, vm2, vm3 must be lonely. n2 is occupied by "other" VMs, so
     * they have to go away before receiving vm3
     *
     * @throws SolverException
     */
    @Test
    public void testFeasibleContinuous() throws SolverException {
        //ChocoLogging.setVerbosity(Verbosity.FINEST);
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n1);
        map.addRunningVM(vm4, n2);
        map.addRunningVM(vm5, n2);

        Set<UUID> mine = new HashSet<UUID>();
        mine.add(vm1);
        mine.add(vm2);
        mine.add(vm3);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        Model mo = new DefaultModel(map);
        Lonely l = new Lonely(mine);
        l.setContinuous(true);
        Set<SatConstraint> cstrs = new HashSet<SatConstraint>();
        cstrs.add(l);
        cstrs.add(new Fence(Collections.singleton(vm3), Collections.singleton(n2)));
        ReconfigurationPlan plan = cra.solve(mo, cstrs);
        Assert.assertNotNull(plan);
        System.out.println(plan);
        Assert.assertEquals(l.isSatisfied(plan.getResult()), SatConstraint.Sat.SATISFIED);
    }

    @Test
    public void testGetMisplaced() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n1);
        map.addRunningVM(vm4, n2);
        map.addRunningVM(vm5, n2);
        Model mo = new DefaultModel(map);

        Set<UUID> mine = new HashSet<UUID>();
        mine.add(vm1);
        mine.add(vm2);
        mine.add(vm3);

        CLonely c = new CLonely(new Lonely(mine));
        Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        map.addRunningVM(vm2, n2);
        Assert.assertEquals(c.getMisPlacedVMs(mo), map.getRunningVMs(n2));
    }
}
