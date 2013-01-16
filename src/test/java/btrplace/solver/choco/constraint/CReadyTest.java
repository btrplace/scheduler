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

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Ready;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link btrplace.solver.choco.constraint.CReady}.
 *
 * @author Fabien Hermenier
 */
public class CReadyTest extends ConstraintTestMaterial {

   /* @Test
    public void testInstantiation() {
        Ready b = new Ready(Collections.singleton(UUID.randomUUID()));
        CReady c = new CReady(b);
        Assert.assertEquals(b, c.getAssociatedConstraint());
    }*/

    @Test
    public void testGetMisplaced() {
        Mapping m = new DefaultMapping();
        m.addReadyVM(vm1);
        m.addOnlineNode(n1);
        m.addRunningVM(vm2, n1);
        m.addSleepingVM(vm3, n1);
        Model mo = new DefaultModel(m);
        CReady k = new CReady(new Ready(m.getAllVMs()));
        Assert.assertEquals(2, k.getMisPlacedVMs(mo).size());
        Assert.assertFalse(k.getMisPlacedVMs(mo).contains(vm1));
    }

    /*@Test
    public void testIsSatisfied() {
        Mapping m = new DefaultMapping();
        Model mo = new DefaultModel(m);
        UUID vm = UUID.randomUUID();

        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        CReady k = new CReady(new Ready(Collections.singleton(vm)));
        Assert.assertFalse(k.isSatisfied(p));
        p.add(new ForgeVM(vm, 1, 2));
        Assert.assertTrue(k.isSatisfied(p));
    }          */
}
