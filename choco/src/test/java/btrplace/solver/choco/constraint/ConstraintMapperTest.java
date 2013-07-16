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

package btrplace.solver.choco.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.constraint.Ban;
import btrplace.model.constraint.Constraint;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.Spread;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
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
    public void testGetBuilder() {
        ConstraintMapper map = ConstraintMapper.newBundle();
        ChocoConstraintBuilder b = map.getBuilder(Spread.class);
        Assert.assertEquals(b.getClass(), CSpread.Builder.class);

        Assert.assertNull(map.getBuilder(MockSatConstraint.class));
    }

    @Test(dependsOnMethods = {"testInstantiate", "testRegister"})
    public void testUnregister() {
        ConstraintMapper map = new ConstraintMapper();
        Assert.assertNull(map.getBuilder(MockSatConstraint.class));
        Assert.assertFalse(map.unregister(MockSatConstraint.class));
    }

    @Test(dependsOnMethods = {"testInstantiate", "testGetBuilder"})
    public void testRegister() {
        ConstraintMapper map = new ConstraintMapper();
        Builder cb = new Builder();
        Assert.assertTrue(map.register(cb));
        Assert.assertEquals(map.getBuilder(MockSatConstraint.class), cb);
    }

    @Test(dependsOnMethods = {"testInstantiate", "testUnregister", "testRegister"})
    public void testMap() {
        Model mo = new DefaultModel();
        ConstraintMapper map = ConstraintMapper.newBundle();
        Spread s = new Spread(Collections.singleton(mo.newVM()));
        ChocoConstraint c = map.map(s);
        Assert.assertTrue(c.getClass().equals(CSpread.class));

        map.unregister(Spread.class);
        CSpread.Builder cb = new CSpread.Builder();
        map.register(cb);
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

    public class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return MockSatConstraint.class;
        }

        @Override
        public ChocoConstraint build(Constraint cstr) {
            throw new UnsupportedOperationException();
        }
    }

    public static class MockCConstraint implements ChocoConstraint {


        @Override
        public boolean inject(ReconfigurationProblem rp) throws SolverException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<VM> getMisPlacedVMs(Model m) {
            throw new UnsupportedOperationException();
        }
    }
}
