/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

;

/**
 * Unit tests for {@link btrplace.model.constraint.Sleeping}.
 *
 * @author Fabien Hermenier
 */
public class SleepingTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<Integer> x = new HashSet<>(Arrays.asList(vm1, vm2));
        Sleeping s = new Sleeping(x);
        Assert.assertNotNull(s.getChecker());
        Assert.assertEquals(x, s.getInvolvedVMs());
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertNotNull(s.toString());
        System.out.println(s);
    }

    @Test
    public void testEquals() {
        Set<Integer> x = new HashSet<>(Arrays.asList(vm1, vm2));
        Sleeping s = new Sleeping(x);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Sleeping(x).equals(s));
        Assert.assertEquals(new Sleeping(x).hashCode(), s.hashCode());
        x = new HashSet<>(Arrays.asList(vm3));
        Assert.assertFalse(new Sleeping(x).equals(s));
    }

    @Test
    public void testIsSatisfied() {
        Mapping c = new DefaultMapping();
        Set<Integer> s = new HashSet<>(Arrays.asList(vm1, vm2));

        c.addOnlineNode(n1);
        c.addSleepingVM(vm1, n1);
        c.addSleepingVM(vm2, n1);
        Sleeping d = new Sleeping(s);
        Model i = new DefaultModel(c);
        Assert.assertEquals(d.isSatisfied(i), true);
        c.addReadyVM(vm1);
        Assert.assertEquals(d.isSatisfied(i), false);
        c.addRunningVM(vm1, n1);
        Assert.assertEquals(d.isSatisfied(i), false);
        c.removeVM(vm1);
        Assert.assertEquals(d.isSatisfied(i), false);
    }
}
