/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
        System.out.println(d.toString());
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
