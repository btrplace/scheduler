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

package org.btrplace.scheduler.choco.duration;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link LinearToAResourceActionDuration}.
 *
 * @author Fabien Hermenier
 */
public class LinearToAResourceDurationTest {

    @Test
    public void testSimple() {
        ShareableResource rc = new ShareableResource("foo", 0, 0);
        Model mo = new DefaultModel();

        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        mo.attach(rc);
        rc.setConsumption(vm1, 3);
        LinearToAResourceActionDuration<VM> d = new LinearToAResourceActionDuration<>("foo", 3);
        Assert.assertEquals(d.getCoefficient(), 3.0);
        Assert.assertEquals(d.getOffset(), 0.0);
        Assert.assertEquals(d.getResourceId(), "foo");
        Assert.assertEquals(d.evaluate(mo, vm1), 9);
        Assert.assertEquals(d.evaluate(mo, vm2), 0);

        d = new LinearToAResourceActionDuration<>("foo", 3, 4);
        Assert.assertEquals(d.evaluate(mo, vm1), 13);
        Assert.assertEquals(d.evaluate(mo, vm3), 4);

        d = new LinearToAResourceActionDuration<>("bar", 3, 4);
        Assert.assertEquals(d.evaluate(mo, vm3), -1);

        d.setCoefficient(5);
        d.setOffset(12);
        d.setResourceId("bar");
        Assert.assertEquals(d.getCoefficient(), 5.0);
        Assert.assertEquals(d.getOffset(), 12.0);
        Assert.assertEquals(d.getResourceId(), "bar");
    }
}
