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

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ConstantActionDuration}.
 *
 * @author Fabien Hermenier
 */
public class ConstantDurationTest {

    @Test
    public void testInstantiate() {
        Model mo = new DefaultModel();
        ConstantActionDuration<Node> cd = new ConstantActionDuration<>(5);
        Assert.assertEquals(5, cd.evaluate(mo, mo.newNode()));
        Assert.assertNotNull(cd.toString());
    }
}
