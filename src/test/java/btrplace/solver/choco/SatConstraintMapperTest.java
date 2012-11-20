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

import btrplace.model.constraint.Ban;
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.Spread;
import btrplace.solver.choco.constraint.ChocoSatContinuousSpread;
import btrplace.solver.choco.constraint.ChocoSatLazySpread;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.UUID;

/**
 * Unit tests for {@link SatConstraintMapper}.
 *
 * @author Fabien Hermenier
 */
public class SatConstraintMapperTest {

    @Test
    public void testInstantiate() {
        SatConstraintMapper map = new SatConstraintMapper();

        //Only check if the default mapper are here
        Assert.assertTrue(map.isRegistered(Spread.class));
        Assert.assertTrue(map.isRegistered(Ban.class));
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testGetBuilder() {
        SatConstraintMapper map = new SatConstraintMapper();
        ChocoConstraintBuilder b = map.getBuilder(Spread.class);
        Assert.assertEquals(b.getClass(), ChocoSatContinuousSpread.Builder.class);

        Assert.assertNull(map.getBuilder(Fence.class));
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testUnregister() {
        SatConstraintMapper map = new SatConstraintMapper();
        Assert.assertTrue(map.unregister(Spread.class));
        Assert.assertNull(map.getBuilder(Spread.class));
        Assert.assertFalse(map.unregister(Spread.class));
    }

    @Test(dependsOnMethods = {"testInstantiate", "testGetBuilder", "testUnregister"})
    public void testRegister() {
        SatConstraintMapper map = new SatConstraintMapper();
        map.unregister(Spread.class);
        ChocoSatLazySpread.Builder cb = new ChocoSatLazySpread.Builder();
        Assert.assertTrue(map.register(cb));
        Assert.assertEquals(map.getBuilder(Spread.class), cb);

        ChocoSatContinuousSpread.Builder cb2 = new ChocoSatContinuousSpread.Builder();
        Assert.assertFalse(map.register(cb2));
        Assert.assertEquals(map.getBuilder(Spread.class), cb2);
    }

    @Test(dependsOnMethods = {"testInstantiate", "testUnregister", "testRegister"})
    public void testMap() {
        SatConstraintMapper map = new SatConstraintMapper();
        Spread s = new Spread(Collections.singleton(UUID.randomUUID()));
        ChocoSatConstraint c = map.map(s);
        Assert.assertTrue(c.getClass().equals(ChocoSatContinuousSpread.class));

        map.unregister(Spread.class);
        ChocoSatLazySpread.Builder cb = new ChocoSatLazySpread.Builder();
        map.register(cb);
        c = map.map(s);
        Assert.assertTrue(c.getClass().equals(ChocoSatLazySpread.class));

        //Fence is not registered !
        Fence b = new Fence(Collections.singleton(UUID.randomUUID()), Collections.singleton(UUID.randomUUID()));
        Assert.assertNull(map.map(b));

    }
}
