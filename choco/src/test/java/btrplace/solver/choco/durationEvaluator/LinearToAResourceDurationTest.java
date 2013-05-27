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
        Model mo = new DefaultModel();
        mo.attach(rc);
        rc.set(vm1, 3);
        LinearToAResourceDuration d = new LinearToAResourceDuration("foo", 3);
        Assert.assertEquals(d.getCoefficient(), 3.0);
        Assert.assertEquals(d.getOffset(), 0.0);
        Assert.assertEquals(d.getResourceId(), "foo");
        Assert.assertEquals(d.evaluate(mo, vm1), 9);
        Assert.assertEquals(d.evaluate(mo, vm2), 0);

        d = new LinearToAResourceDuration("foo", 3, 4);
        Assert.assertEquals(d.evaluate(mo, vm1), 13);
        Assert.assertEquals(d.evaluate(mo, vm3), 4);

        d = new LinearToAResourceDuration("bar", 3, 4);
        Assert.assertEquals(d.evaluate(mo, vm3), -1);

        d.setCoefficient(5);
        d.setOffset(12);
        d.setResourceId("bar");
        Assert.assertEquals(d.getCoefficient(), 5.0);
        Assert.assertEquals(d.getOffset(), 12.0);
        Assert.assertEquals(d.getResourceId(), "bar");
    }
}
