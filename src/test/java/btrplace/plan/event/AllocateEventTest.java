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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link AllocateEvent}.
 *
 * @author Fabien Hermenier
 */
public class AllocateEventTest {

    @Test
    public void testBasics() {
        UUID vm = UUID.randomUUID();
        AllocateEvent na = new AllocateEvent(vm, "foo", 3);
        Assert.assertEquals(vm, na.getVM());
        Assert.assertEquals("foo", na.getResourceId());
        Assert.assertEquals(3, na.getAmount());
        Assert.assertFalse(na.toString().contains("null"));

    }

    @Test
    public void testEqualsHashCode() {
        UUID vm = UUID.randomUUID();
        AllocateEvent na = new AllocateEvent(vm, "foo", 3);
        AllocateEvent na2 = new AllocateEvent(vm, "foo", 3);
        Assert.assertTrue(na.equals(na2));
        Assert.assertTrue(na2.equals(na));
        Assert.assertEquals(na.hashCode(), na2.hashCode());
        Assert.assertFalse(na.equals(new AllocateEvent(UUID.randomUUID(), "foo", 3)));
        Assert.assertFalse(na.equals(new AllocateEvent(vm, "bar", 3)));
        Assert.assertFalse(na.equals(new AllocateEvent(vm, "foo", 5)));
    }
}
