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
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link Killed}.
 *
 * @author Fabien Hermenier
 */
public class KilledTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<UUID> x = new HashSet<UUID>(Arrays.asList(vm1, vm2));
        Killed s = new Killed(x);
        Assert.assertEquals(x, s.getInvolvedVMs());
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertNotNull(s.toString());
        System.out.println(s);
    }

    @Test
    public void testEquals() {
        Set<UUID> x = new HashSet<UUID>(Arrays.asList(vm1, vm2));
        Killed s = new Killed(x);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Killed(x).equals(s));
        Assert.assertEquals(new Killed(x).hashCode(), s.hashCode());
        x = new HashSet<UUID>(Arrays.asList(vm3));
        Assert.assertFalse(new Killed(x).equals(s));
    }

    @Test
    public void testIsSatisfied() {
        Mapping c = new DefaultMapping();
        Set<UUID> s = new HashSet<UUID>(Arrays.asList(vm1, vm2));
        Killed d = new Killed(s);
        Model i = new DefaultModel(c);
        Assert.assertEquals(d.isSatisfied(i), SatConstraint.Sat.SATISFIED);
        c.addReadyVM(vm1);
        Assert.assertEquals(d.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);
        c.addOnlineNode(n1);
        c.addRunningVM(vm1, n1);
        Assert.assertEquals(d.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);
        c.addSleepingVM(vm1, n1);
        Assert.assertEquals(d.isSatisfied(i), SatConstraint.Sat.UNSATISFIED);
    }
}
