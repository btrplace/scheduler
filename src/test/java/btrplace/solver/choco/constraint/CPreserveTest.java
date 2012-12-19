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
import btrplace.model.constraint.Preserve;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link CPreserve}.
 *
 * @author Fabien Hermenier
 */
public class CPreserveTest {

    /*@Test
    public void testInstantiation() {
        Preserve b = new Preserve(Collections.singleton(UUID.randomUUID()), "cpu", 5);
        CPreserve c = new CPreserve(b);
        Assert.assertEquals(b, c.getAssociatedConstraint());
    } */

    @Test
    public void testGetMisplaced() {
        Mapping map = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        ShareableResource rc = new DefaultShareableResource("cpu", 7);
        rc.set(vm1, 3);
        rc.set(vm2, 3);
        rc.set(vm3, 5);

        Preserve p = new Preserve(map.getAllVMs(), "cpu", 5);

        Model mo = new DefaultModel(map);
        mo.attach(rc);
        Assert.assertEquals(SatConstraint.Sat.UNSATISFIED, p.isSatisfied(mo));

        CPreserve cp = new CPreserve(p);
        Set<UUID> bads = cp.getMisPlacedVMs(mo);
        Assert.assertEquals(map.getRunningVMs(n1), bads);
    }

    /**
     * A preserve constraint asks for a minimum amount of resources but
     * their is no overbook ratio so, their should be no relocation,
     * but also no allocate action ?
     * TODO: Consistent or not ?
     *
     * @throws SolverException
     */
    @Test
    public void testPreserveWithoutOverbook() throws SolverException {
        Mapping map = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n2);
        ShareableResource rc = new DefaultShareableResource("cpu", 10);
        rc.set(n1, 7);
        rc.set(vm1, 3);
        rc.set(vm2, 3);
        rc.set(vm3, 5);

        Preserve pr = new Preserve(map.getAllVMs(), "cpu", 5);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        Model mo = new DefaultModel(map);
        mo.attach(rc);
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(new Online(map.getAllNodes()));
        cstrs.add(pr);
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        //No relocation
        Assert.assertEquals(p.getResult().getMapping(), map);

    }
}
