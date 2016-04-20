/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
import org.btrplace.model.constraint.Ban;
import org.btrplace.model.constraint.Spread;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;


/**
 * Unit tests for {@link ChocoMapper}.
 *
 * @author Fabien Hermenier
 */
public class ChocoMapperTest {

    @Test
    public void testInstantiate() {
        ChocoMapper map = ChocoMapper.newBundle();

        //Only check if the default mapper are here
        Assert.assertTrue(map.constraintHasMapping(Spread.class));
        Assert.assertTrue(map.constraintHasMapping(Ban.class));
    }

    @Test(dependsOnMethods = {"testInstantiate"})
    public void testMap() {
        Model mo = new DefaultModel();
        ChocoMapper map = ChocoMapper.newBundle();
        Spread s = new Spread(Collections.singleton(mo.newVM()));
        ChocoConstraint c = map.get(s);
        Assert.assertTrue(c.getClass().equals(CSpread.class));

        map.unMapConstraint(Spread.class);
        map.mapConstraint(Spread.class, CSpread.class);
        c = map.get(s);
        Assert.assertTrue(c.getClass().equals(CSpread.class));
    }
}
