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

package btrplace.plan.event;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.view.ShareableResource;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link Allocate}.
 *
 * @author Fabien Hermenier
 */
public class AllocateTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Allocate a = new Allocate(vm1, n1, "foo", 3, 1, 5);
        Assert.assertEquals(vm1, a.getVM());
        Assert.assertEquals(n1, a.getHost());
        Assert.assertEquals("foo", a.getResourceId());
        Assert.assertEquals(3, a.getAmount());
        Assert.assertEquals(1, a.getStart());
        Assert.assertEquals(5, a.getEnd());
        Assert.assertFalse(a.toString().contains("null"));
    }

    @Test
    public void testApply() {
        Allocate na = new Allocate(vm1, n2, "foo", 3, 3, 5);
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        Model mo = new DefaultModel(map);
        Assert.assertFalse(na.apply(mo));
        ShareableResource rc = new ShareableResource("foo");
        mo.attach(rc);
        Assert.assertTrue(na.apply(mo));
        Assert.assertEquals(3, rc.get(vm1));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEquals() {
        Allocate a = new Allocate(vm1, n1, "foo", 5, 3, 5);
        Allocate b = new Allocate(vm1, n1, "foo", 5, 3, 5);
        Assert.assertFalse(a.equals(new Object()));
        Assert.assertTrue(a.equals(a));
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertNotSame(a, new Allocate(vm3, n1, "foo", 5, 3, 5));
        Assert.assertNotSame(a, new Allocate(vm1, n2, "foo", 5, 3, 5));
        Assert.assertNotSame(a, new Allocate(vm1, n1, "bar", 5, 3, 5));
        Assert.assertNotSame(a, new Allocate(vm1, n1, "foo", 6, 3, 5));
        Assert.assertNotSame(a, new Allocate(vm1, n1, "foo", 5, 4, 5));
        Assert.assertNotSame(a, new Allocate(vm1, n1, "foo", 5, 3, 7));
    }

}
