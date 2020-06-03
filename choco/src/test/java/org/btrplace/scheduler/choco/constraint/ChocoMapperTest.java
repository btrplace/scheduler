/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
