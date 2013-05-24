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

package btrplace.solver.choco.durationEvaluator;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ConstantDuration}.
 *
 * @author Fabien Hermenier
 */
public class ConstantDurationTest implements PremadeElements {

    @Test
    public void testInstantiate() {
        ConstantDuration cd = new ConstantDuration(5);
        Assert.assertEquals(5, cd.evaluate(new DefaultModel(new DefaultMapping()), vm1));
        Assert.assertNotNull(cd.toString());
    }
}
