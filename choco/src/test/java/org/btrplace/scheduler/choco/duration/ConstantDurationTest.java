/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.duration;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
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
