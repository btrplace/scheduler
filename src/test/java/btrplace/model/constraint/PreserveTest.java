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

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.view.DefaultShareableResource;
import btrplace.model.view.ShareableResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link Preserve}.
 *
 * @author Fabien Hermenier
 */
public class PreserveTest {

    @Test
    public void testInstantiation() {
        Set<UUID> vms = new HashSet<UUID>();
        vms.add(UUID.randomUUID());
        vms.add(UUID.randomUUID());
        Preserve p = new Preserve(vms, "cpu", 3);
        Assert.assertEquals(vms, p.getInvolvedVMs());
        Assert.assertTrue(p.getInvolvedNodes().isEmpty());
        Assert.assertEquals(3, p.getAmount());
        Assert.assertEquals("cpu", p.getResource());
        Assert.assertFalse(p.toString().contains("null"));
        Assert.assertFalse(p.isContinuous());
        Assert.assertFalse(p.setContinuous(true));
        System.out.println(p);
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEqualsAndHashCode() {
        Set<UUID> vms = new HashSet<UUID>();
        vms.add(UUID.randomUUID());
        vms.add(UUID.randomUUID());
        Preserve p = new Preserve(vms, "cpu", 3);
        Preserve p2 = new Preserve(vms, "cpu", 3);
        Assert.assertTrue(p.equals(p));
        Assert.assertTrue(p2.equals(p));
        Assert.assertEquals(p2.hashCode(), p.hashCode());
        Assert.assertFalse(new Preserve(vms, "mem", 3).equals(p));
        Assert.assertFalse(new Preserve(vms, "cpu", 2).equals(p));
        Assert.assertFalse(new Preserve(new HashSet<UUID>(), "cpu", 3).equals(p));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testIsSatisfied() {
        ShareableResource rc = new DefaultShareableResource("cpu");
        Model m = new DefaultModel(new DefaultMapping());
        m.attach(rc);
        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        Set<UUID> s = new HashSet<UUID>();
        s.add(vm1);
        s.add(vm2);
        s.add(vm3);
        Preserve p = new Preserve(s, "cpu", 3);
        rc.set(vm1, 3);
        rc.set(vm2, 4);
        rc.set(vm3, 3);
        Assert.assertEquals(SatConstraint.Sat.SATISFIED, p.isSatisfied(m));

        rc.unset(vm3); //Set to 0 by default
        Assert.assertEquals(SatConstraint.Sat.UNSATISFIED, p.isSatisfied(m));
        Assert.assertEquals(SatConstraint.Sat.UNSATISFIED, new Preserve(s, "mem", 3).isSatisfied(m));
    }
}
