/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.duration;

import org.btrplace.model.Attributes;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ActionDurationFromOptionalAttribute}.
 *
 * @author Fabien Hermenier
 */
public class DurationFromOptionalAttributeTest {

    @Test
    public void test() {


        Model mo = new DefaultModel();
        Attributes attrs = mo.getAttributes();
        ActionDurationEvaluator<VM> parent = new ConstantActionDuration<>(15);
        VM vm1 = mo.newVM();
        ActionDurationFromOptionalAttribute<VM> dev = new ActionDurationFromOptionalAttribute<>("boot", parent);
        Assert.assertEquals(parent, dev.getParent());
        Assert.assertEquals(dev.getAttributeKey(), "boot");
        Assert.assertEquals(dev.evaluate(mo, vm1), 15);

        attrs.put(vm1, "boot", 7);
        Assert.assertEquals(dev.evaluate(mo, vm1), 7);

        parent = new ConstantActionDuration<>(2);
        dev.setParent(parent);
        attrs.clear();
        Assert.assertEquals(dev.evaluate(mo, vm1), 2);
        Assert.assertEquals(parent, dev.getParent());
        Assert.assertFalse(dev.toString().contains("null"));

    }
}
