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
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Root;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.action.MigrateVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link CRoot}.
 *
 * @author Fabien Hermenier
 */
public class CRootTest {

    @Test
    public void testInstantiation() {
        Root b = new Root(Collections.singleton(UUID.randomUUID()));
        CRoot c = new CRoot(b);
        Assert.assertEquals(b, c.getAssociatedConstraint());
    }

    @Test
    public void testBasic() throws SolverException {
        Mapping map = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        map.addSleepingVM(vm2, n1);
        map.addReadyVM(vm3);

        Model mo = new DefaultModel(map);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.repair(false);
        Root r1 = new Root(map.getAllVMs());
        Online n = new Online(map.getAllNodes());
        List<SatConstraint> l = new ArrayList<SatConstraint>();
        l.add(r1);
        l.add(n);
        ReconfigurationPlan p = cra.solve(mo, l);
        Assert.assertNotNull(p);
        Model res = p.getResult();
        Assert.assertEquals(n1, res.getMapping().getVMLocation(vm1));
    }

    @Test
    public void testIsSatisfied() {
        Mapping map = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID vm1 = UUID.randomUUID();
        Model mo = new DefaultModel(map);
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        CRoot r = new CRoot(new Root(Collections.singleton(vm1)));
        Assert.assertTrue(r.isSatisfied(p));
        Assert.assertTrue(r.getMisPlacedVMs(mo).isEmpty());
        p.add(new MigrateVM(vm1, n1, n2, 1, 2));
        Assert.assertFalse(r.isSatisfied(p));
    }
}
