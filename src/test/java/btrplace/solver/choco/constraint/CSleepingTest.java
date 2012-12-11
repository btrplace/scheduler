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
import btrplace.model.constraint.Sleeping;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.SuspendVM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.UUID;

/**
 * Unit tests for {@link CSleeping}.
 *
 * @author Fabien Hermenier
 */
public class CSleepingTest {

    @Test
    public void testInstantiation() {
        Sleeping b = new Sleeping(Collections.singleton(UUID.randomUUID()));
        CSleeping c = new CSleeping(b);
        Assert.assertEquals(b, c.getAssociatedConstraint());
    }

    @Test
    public void testGetMisplaced() {
        Mapping m = new DefaultMapping();
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        m.addReadyVM(vm1);
        UUID n1 = UUID.randomUUID();
        m.addOnlineNode(n1);
        m.addRunningVM(vm2, n1);
        m.addSleepingVM(vm3, n1);
        Model mo = new DefaultModel(m);
        CSleeping k = new CSleeping(new Sleeping(m.getAllVMs()));
        Assert.assertEquals(2, k.getMisPlacedVMs(mo).size());
        Assert.assertFalse(k.getMisPlacedVMs(mo).contains(vm3));
    }

    @Test
    public void testIsSatisfied() {
        Mapping m = new DefaultMapping();
        Model mo = new DefaultModel(m);
        UUID vm = UUID.randomUUID();
        UUID n1 = UUID.randomUUID();
        m.addOnlineNode(n1);
        m.addRunningVM(vm, n1);
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        CSleeping k = new CSleeping(new Sleeping(Collections.singleton(vm)));
        Assert.assertFalse(k.isSatisfied(p));
        p.add(new SuspendVM(vm, n1, n1, 1, 2));
        Assert.assertTrue(k.isSatisfied(p));
    }
}
