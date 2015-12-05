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

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Ban;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.Spread;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Set;


/**
 * Unit tests for {@link ConstraintMapper}.
 *
 * @author Fabien Hermenier
 */
public class ConstraintMapperTest {

    @Test
    public void testInstantiate() {
        ConstraintMapper map = ConstraintMapper.newBundle();

        //Only check if the default mapper are here
        Assert.assertTrue(map.isRegistered(Spread.class));
        Assert.assertTrue(map.isRegistered(Ban.class));
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testRegister() {
        ConstraintMapper map = new ConstraintMapper();
        map.register(MockSatConstraint.class, MockCConstraint.class);
        Assert.assertTrue(map.isRegistered(MockSatConstraint.class));
    }


    @Test(dependsOnMethods = {"testInstantiate", "testRegister"})
    public void testUnregister() {
        ConstraintMapper map = new ConstraintMapper();
        Assert.assertFalse(map.isRegistered(MockSatConstraint.class));
        Assert.assertFalse(map.unRegister(MockSatConstraint.class));
    }

    @Test(dependsOnMethods = {"testInstantiate", "testUnregister", "testRegister"})
    public void testMap() {
        Model mo = new DefaultModel();
        ConstraintMapper map = ConstraintMapper.newBundle();
        Spread s = new Spread(Collections.singleton(mo.newVM()));
        ChocoConstraint c = map.map(s);
        Assert.assertTrue(c.getClass().equals(CSpread.class));

        map.unRegister(Spread.class);
        map.register(Spread.class, CSpread.class);
        c = map.map(s);
        Assert.assertTrue(c.getClass().equals(CSpread.class));

        MockSatConstraint b = new MockSatConstraint();
        Assert.assertNull(map.map(b));
    }

    public static class MockSatConstraint extends SatConstraint {
        public MockSatConstraint() {
            super(null, null, false);
        }

        @Override
        public boolean isSatisfied(Model i) {
            throw new UnsupportedOperationException();
        }
    }

    public static class MockCConstraint implements ChocoConstraint {

        public MockCConstraint(MockSatConstraint m) {
        }

        @Override
        public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<VM> getMisPlacedVMs(Model m) {
            throw new UnsupportedOperationException();
        }
    }
}
