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

package btrplace.model.constraint;

import btrplace.model.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link Ready}.
 *
 * @author Fabien Hermenier
 */
public class ReadyTest extends ConstraintTestMaterial {

    @Test
    public void testInstantiation() {
        Set<UUID> x = new HashSet<UUID>(Arrays.asList(vm1, vm2));
        Ready s = new Ready(x);
        Assert.assertEquals(x, s.getInvolvedVMs());
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertNotNull(s.toString());
        System.out.println(s);
    }

    @Test
    public void testEquals() {
        Set<UUID> x = new HashSet<UUID>(Arrays.asList(vm1, vm2));
        Ready s = new Ready(x);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Ready(x).equals(s));
        Assert.assertEquals(new Ready(x).hashCode(), s.hashCode());
        x = new HashSet<UUID>(Arrays.asList(vm3));
        Assert.assertFalse(new Ready(x).equals(s));
    }

    @Test
    public void testIsSatisfied() {
        Mapping c = new DefaultMapping();
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(vm1, vm2));
        c.addOnlineNode(n1);
        c.addReadyVM(vm1);
        c.addReadyVM(vm2);
        Ready d = new Ready(s);
        Model i = new DefaultModel(c);
        Assert.assertEquals(d.isSatisfied(i), SatConstraint.Sat.SATISFIED);
        c.addRunningVM(vm1, n1);
        Assert.assertEquals(d.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);
        c.addSleepingVM(vm1, n1);
        Assert.assertEquals(d.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);
        c.removeVM(vm1);
        Assert.assertEquals(d.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);
    }
}
