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

import btrplace.model.*;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link btrplace.model.constraint.Sleeping}.
 *
 * @author Fabien Hermenier
 */
public class SleepingTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 3);

        Set<VM> x = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1)));
        Sleeping s = new Sleeping(x);
        Assert.assertNotNull(s.getChecker());
        Assert.assertEquals(x, s.getInvolvedVMs());
        Assert.assertTrue(s.getInvolvedNodes().isEmpty());
        Assert.assertNotNull(s.toString());
        System.out.println(s);
    }

    @Test
    public void testEquals() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 3);
        Set<VM> x = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1)));
        Sleeping s = new Sleeping(x);

        Assert.assertTrue(s.equals(s));
        Assert.assertTrue(new Sleeping(x).equals(s));
        Assert.assertEquals(new Sleeping(x).hashCode(), s.hashCode());
        x = new HashSet<>(Arrays.asList(vms.get(2)));
        Assert.assertFalse(new Sleeping(x).equals(s));
    }

    @Test
    public void testIsSatisfied() {
        Model i = new DefaultModel();
        List<VM> vms = Util.newVMs(i, 3);
        List<Node> ns = Util.newNodes(i, 3);

        Mapping c = i.getMapping();
        Set<VM> s = new HashSet<>(Arrays.asList(vms.get(0), vms.get(1)));

        c.addOnlineNode(ns.get(0));
        c.addSleepingVM(vms.get(0), ns.get(0));
        c.addSleepingVM(vms.get(1), ns.get(0));
        Sleeping d = new Sleeping(s);
        Assert.assertEquals(d.isSatisfied(i), true);
        c.addReadyVM(vms.get(0));
        Assert.assertEquals(d.isSatisfied(i), false);
        c.addRunningVM(vms.get(0), ns.get(0));
        Assert.assertEquals(d.isSatisfied(i), false);
        c.remove(vms.get(0));
        Assert.assertEquals(d.isSatisfied(i), false);
    }
}
