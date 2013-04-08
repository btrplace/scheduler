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

package btrplace.solver.choco.durationEvaluator;

import btrplace.model.view.ShareableResource;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link LinearToAResourceDuration}.
 *
 * @author Fabien Hermenier
 */
public class LinearToAResourceDurationTest implements PremadeElements {

    @Test
    public void testSimple() {
        ShareableResource rc = new ShareableResource("foo", 0);
        rc.set(vm1, 3);
        LinearToAResourceDuration d = new LinearToAResourceDuration(rc, 3);
        Assert.assertEquals(d.evaluate(vm1), 9);
        Assert.assertEquals(d.evaluate(vm2), 0);

        d = new LinearToAResourceDuration(rc, 3, 4);
        Assert.assertEquals(d.evaluate(vm1), 13);
        Assert.assertEquals(d.evaluate(vm2), 4);
    }
}
