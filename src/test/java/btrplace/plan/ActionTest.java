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

package btrplace.plan;

import btrplace.model.Model;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link Action}.
 *
 * @author Fabien Hermenier
 */
public class ActionTest {

    public static class MockAction extends Action {

        public MockAction(int st, int ed) {
            super(st, ed);
        }

        @Override
        public boolean applyAction(Model i) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String pretty() {
            return "pretty()";
        }
    }

    public static class MockEvent implements Event {

        @Override
        public boolean apply(Model m) {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            return "event()";
        }
    }

    @Test
    public void testBasics() {
        Action a1 = new MockAction(1, 3);
        Assert.assertEquals(1, a1.getStart());
        Assert.assertEquals(3, a1.getEnd());
        Assert.assertTrue(a1.getEvents(Action.Hook.pre).isEmpty());
        Assert.assertTrue(a1.getEvents(Action.Hook.post).isEmpty());
    }

    @Test
    public void testEvents() {
        Action a1 = new MockAction(1, 3);
        MockEvent n1 = new MockEvent();
        a1.addEvent(Action.Hook.pre, n1);
        Assert.assertEquals(1, a1.getEvents(Action.Hook.pre).size());
        a1.addEvent(Action.Hook.post, n1);
        Assert.assertEquals(1, a1.getEvents(Action.Hook.post).size());
        System.out.println(a1);
    }
}
