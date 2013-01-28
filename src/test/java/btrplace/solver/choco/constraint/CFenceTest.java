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
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Ready;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.MigrateVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link btrplace.model.constraint.Fence}.
 *
 * @author Fabien Hermenier
 */
public class CFenceTest extends ConstraintTestMaterial {

    /**
     * Test getMisPlaced() in various situations.
     */
    @Test
    public void testGetMisPlaced() {
        Mapping m = new DefaultMapping();

        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);
        m.addOnlineNode(n4);
        m.addOfflineNode(n5);

        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm2, n1);
        m.addRunningVM(vm3, n2);
        m.addRunningVM(vm4, n3);
        m.addSleepingVM(vm5, n4);

        Set<UUID> vms = new HashSet<UUID>();
        Set<UUID> ns = new HashSet<UUID>();

        vms.add(vm1);
        vms.add(vm2);
        ns.add(n1);
        ns.add(n2);
        CFence c = new CFence(new Fence(vms, ns));
        Model mo = new DefaultModel(m);
        Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        ns.add(vm5);
        Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        vms.add(vm3);
        Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        vms.add(vm4);
        Set<UUID> bad = c.getMisPlacedVMs(mo);
        Assert.assertEquals(1, bad.size());
        Assert.assertTrue(bad.contains(vm4));
    }

    @Test
    public void testBasic() throws SolverException {
        Mapping map = new DefaultMapping();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n3);
        map.addRunningVM(vm4, n1);

        Set<UUID> on = new HashSet<UUID>();
        on.add(n3);
        on.add(n1);
        Fence f = new Fence(map.getAllVMs(), on);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(f);
        cstrs.add(new Online(map.getAllNodes()));
        Model mo = new DefaultModel(map);
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getSize() > 0);
        Assert.assertTrue(p.iterator().next() instanceof MigrateVM);
        Assert.assertEquals(SatConstraint.Sat.SATISFIED, f.isSatisfied(p.getResult()));
        cstrs.add(new Ready(Collections.singleton(vm2)));

        p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        Assert.assertEquals(1, p.getSize()); //Just the suspend of vm2
        Assert.assertEquals(SatConstraint.Sat.SATISFIED, f.isSatisfied(p.getResult()));


    }
}
