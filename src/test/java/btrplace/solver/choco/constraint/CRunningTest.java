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
import btrplace.model.constraint.Running;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link btrplace.solver.choco.constraint.CRunning}.
 *
 * @author Fabien Hermenier
 */
public class CRunningTest extends ConstraintTestMaterial {

    /*@Test
    public void testInstantiation() {
        Running b = new Running(Collections.singleton(UUID.randomUUID()));
        CRunning c = new CRunning(b);
        Assert.assertEquals(b, c.getAssociatedConstraint());
    } */

    @Test
    public void testGetMisplaced() {
        Mapping m = new DefaultMapping();
        m.addReadyVM(vm1);
        m.addOnlineNode(n1);
        m.addRunningVM(vm2, n1);
        Model mo = new DefaultModel(m);

        CRunning k = new CRunning(new Running(m.getAllVMs()));
        Assert.assertEquals(1, k.getMisPlacedVMs(mo).size());
        Assert.assertTrue(k.getMisPlacedVMs(mo).contains(vm1));
    }

    /*@Test
    public void testIsSatisfied() {
        Mapping m = new DefaultMapping();
        Model mo = new DefaultModel(m);
        UUID vm = UUID.randomUUID();
        UUID n1 = UUID.randomUUID();
        m.addOnlineNode(n1);
        m.addSleepingVM(vm, n1);
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        CRunning k = new CRunning(new Running(Collections.singleton(vm)));
        Assert.assertFalse(k.isSatisfied(p));
        p.add(new ResumeVM(vm, n1, n1, 1, 2));
        Assert.assertTrue(k.isSatisfied(p));

        vm = UUID.randomUUID();
        m.addReadyVM(vm);
        p.add(new BootVM(vm, n1, 1, 2));
        k = new CRunning(new Running(Collections.singleton(vm)));
        Assert.assertTrue(k.isSatisfied(p));
    }         */
}
