/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.plan;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.plan.event.Action;
import btrplace.plan.event.Event;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link btrplace.plan.event.Action}.
 *
 * @author Fabien Hermenier
 */
public class ActionTest {

    @Test
    public void testBasics() {
        Action a1 = new MockAction(new VM(1), 1, 3);
        Assert.assertEquals(1, a1.getStart());
        Assert.assertEquals(3, a1.getEnd());
        Assert.assertTrue(a1.getEvents(Action.Hook.PRE).isEmpty());
        Assert.assertTrue(a1.getEvents(Action.Hook.POST).isEmpty());
    }

    @Test
    public void testEvents() {
        Action a1 = new MockAction(new VM(1), 1, 3);
        Event e = mock(Event.class);
        a1.addEvent(Action.Hook.PRE, e);
        Assert.assertEquals(1, a1.getEvents(Action.Hook.PRE).size());
        a1.addEvent(Action.Hook.POST, e);
        Assert.assertEquals(1, a1.getEvents(Action.Hook.POST).size());
    }

    @Test
    public void testApply() {
        Model mo = new DefaultModel();

        MockAction a1 = new MockAction(new VM(1), 1, 3);
        Event e = mock(Event.class);
        when(e.apply(mo)).thenReturn(true);
        a1.addEvent(Action.Hook.PRE, e);
        a1.addEvent(Action.Hook.POST, e);

        a1.apply(mo);

        verify(e, times(2)).apply(mo);
        Assert.assertEquals(a1.count, 1);
    }
}
