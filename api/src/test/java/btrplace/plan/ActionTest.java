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

package btrplace.plan;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.event.Action;
import btrplace.plan.event.ActionVisitor;
import btrplace.plan.event.Event;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link btrplace.plan.event.Action}.
 *
 * @author Fabien Hermenier
 */
public class ActionTest {

    static Model mo = new DefaultModel();
    VM vm = mo.newVM();
    Node n = mo.newNode();

    public static class MockEvent implements Event {

        int count = 0;

        @Override
        public boolean apply(Model m) {
            count++;
            return true;
        }

        public String toString() {
            return "event()";
        }

        @Override
        public Object visit(ActionVisitor v) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void testBasics() {
        Action a1 = new MockAction(vm, 1, 3);
        Assert.assertEquals(1, a1.getStart());
        Assert.assertEquals(3, a1.getEnd());
        Assert.assertTrue(a1.getEvents(Action.Hook.pre).isEmpty());
        Assert.assertTrue(a1.getEvents(Action.Hook.post).isEmpty());
    }

    @Test
    public void testEvents() {
        Action a1 = new MockAction(vm, 1, 3);
        MockEvent m = new MockEvent();
        a1.addEvent(Action.Hook.pre, m);
        Assert.assertEquals(1, a1.getEvents(Action.Hook.pre).size());
        a1.addEvent(Action.Hook.post, m);
        Assert.assertEquals(1, a1.getEvents(Action.Hook.post).size());
        //System.out.println(a1);
    }

    @Test
    public void testApply() {
        MockAction a1 = new MockAction(vm, 1, 3);
        MockEvent m = new MockEvent();
        a1.addEvent(Action.Hook.pre, m);
        a1.addEvent(Action.Hook.post, m);
        Model mo = new DefaultModel();
        a1.apply(mo);
        Assert.assertEquals(m.count, 2);
        Assert.assertEquals(a1.count, 1);
    }
}
