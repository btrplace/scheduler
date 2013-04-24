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

package btrplace.solver.choco;

import btrplace.model.ModelView;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.view.CShareableResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link ModelViewMapper}.
 *
 * @author Fabien Hermenier
 */
public class ModelViewMapperTest {

    @Test
    public void testInstantiate() {
        ModelViewMapper map = new ModelViewMapper();
        Assert.assertTrue(map.isRegistered(ShareableResource.class));
        Assert.assertTrue(map.getBuilder(ShareableResource.class) instanceof CShareableResource.Builder);
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testRegister() {
        ModelViewMapper map = new ModelViewMapper();
        Builder cb = new Builder();
        Assert.assertTrue(map.register(cb));
        Assert.assertEquals(map.getBuilder(MockModelView.class), cb);
    }

    @Test(dependsOnMethods = {"testInstantiate", "testRegister"})
    public void testUnregister() {
        ModelViewMapper map = new ModelViewMapper();
        Assert.assertNull(map.getBuilder(MockModelView.class));
        Assert.assertFalse(map.unregister(MockModelView.class));
    }

    @Test(dependsOnMethods = {"testInstantiate", "testUnregister", "testRegister"})
    public void testMap() throws SolverException {
        ModelViewMapper map = new ModelViewMapper();
        map.register(new Builder());
        MockModelView v = new MockModelView();
        ChocoModelView cv = map.map(null, v);
        Assert.assertTrue(cv.getClass().equals(CMockModelView.class));

        map.unregister(MockModelView.class);
        Assert.assertNull(map.map(null, v));
    }

    public static class CMockModelView implements ChocoModelView {
        @Override
        public String getIdentifier() {
            return "mock";
        }

        @Override
        public boolean beforeSolve(ReconfigurationProblem rp) {
            return true;
        }

        @Override
        public boolean insertActions(ReconfigurationProblem rp, ReconfigurationPlan p) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean cloneVM(UUID vm, UUID clone) {
            throw new UnsupportedOperationException();
        }
    }

    public class Builder implements ChocoModelViewBuilder {
        @Override
        public Class<? extends ModelView> getKey() {
            return MockModelView.class;
        }

        @Override
        public ChocoModelView build(ReconfigurationProblem rp, ModelView v) throws SolverException {
            return new CMockModelView();
        }
    }

    public static class MockModelView implements ModelView {

        @Override
        public String getIdentifier() {
            return "mock";
        }

        @Override
        public ModelView clone() {
            throw new UnsupportedOperationException();
        }
    }
}
