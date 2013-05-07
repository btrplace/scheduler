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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link AllocateEvent}.
 *
 * @author Fabien Hermenier
 */
public class AllocateEventTest implements PremadeElements {

    static AllocateEvent a = new AllocateEvent(vm1, "foo", 3);

    @Test
    public void testBasics() {
        AllocateEvent na = new AllocateEvent(vm1, "foo", 3);
        Assert.assertEquals(vm1, na.getVM());
        Assert.assertEquals("foo", na.getResourceId());
        Assert.assertEquals(3, na.getAmount());
        Assert.assertFalse(na.toString().contains("null"));

    }

    @Test
    public void testEqualsHashCode() {
        AllocateEvent na = new AllocateEvent(vm1, "foo", 3);
        AllocateEvent na2 = new AllocateEvent(vm1, "foo", 3);
        Assert.assertFalse(na.equals(new Object()));
        Assert.assertTrue(na.equals(na));
        Assert.assertTrue(na.equals(na2));
        Assert.assertTrue(na2.equals(na));
        Assert.assertEquals(na.hashCode(), na2.hashCode());
        Assert.assertFalse(na.equals(new AllocateEvent(vm2, "foo", 3)));
        Assert.assertFalse(na.equals(new AllocateEvent(vm1, "bar", 3)));
        Assert.assertFalse(na.equals(new AllocateEvent(vm1, "foo", 5)));
    }

    @Test
    public void testApply() {
        AllocateEvent na = new AllocateEvent(vm1, "foo", 3);
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

    @Test
    public void testVisit() {
        ActionVisitor visitor = mock(ActionVisitor.class);
        a.visit(visitor);
        verify(visitor).visit(a);
    }
}
