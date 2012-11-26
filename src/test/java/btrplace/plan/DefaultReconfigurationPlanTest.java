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

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link DefaultReconfigurationPlan}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlanTest {

    @Test
    public void testInstantiate() {
        Model m = new DefaultModel(new DefaultMapping());
        DefaultReconfigurationPlan p = new DefaultReconfigurationPlan(m);
        Assert.assertEquals(m, p.getOrigin());
        Assert.assertEquals(m, p.getResult());
        Assert.assertEquals(0, p.getDuration());
        Assert.assertTrue(p.getActions().isEmpty());
        Assert.assertFalse(p.toString().contains("null"));

    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testAddDurationAndSize() {
        Model m = new DefaultModel(new DefaultMapping());
        DefaultReconfigurationPlan p = new DefaultReconfigurationPlan(m);
        Action a1 = new MockAction(UUID.randomUUID(), 1, 3);
        Action a2 = new MockAction(UUID.randomUUID(), 2, 4);
        Action a3 = new MockAction(UUID.randomUUID(), 2, 4);
        Action a4 = new MockAction(UUID.randomUUID(), 1, 3);
        Assert.assertTrue(p.add(a1));
        Assert.assertEquals(3, p.getDuration());
        Assert.assertTrue(p.add(a4));
        Assert.assertTrue(p.add(a3));
        Assert.assertTrue(p.add(a2));
        Assert.assertEquals(4, p.getDuration());
        int last = -1;
        for (Action a : p) {
            Assert.assertTrue(a.getStart() >= last);
            last = a.getStart();
        }
        Assert.assertFalse(p.add(a2));

        Assert.assertEquals(4, p.size());

        Assert.assertFalse(p.toString().contains("null"));
    }


    static class MockAction extends Action {

        private UUID e;

        public MockAction(UUID elmt, int st, int ed) {
            super(st, ed);
            e = elmt;
        }

        public boolean equals(Object o) {
            return o instanceof MockAction && ((MockAction) o).e.equals(e);
        }

        @Override
        public boolean apply(Model i) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
