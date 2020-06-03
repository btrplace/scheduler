/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Basic test for {@link SchedulerException}.
 *
 * @author Fabien Hermenier
 */
public class SchedulerExceptionTest {

    @Test
    public void testBasic() {
        Model mo = new DefaultModel();
        SchedulerException ex = new SchedulerException(mo, "foo");
        Assert.assertEquals(ex.getModel(), mo);
        Assert.assertEquals(ex.getMessage(), "foo");

        SchedulerException ex2 = new SchedulerException(mo, "foo", ex);
        Assert.assertEquals(ex2.getModel(), mo);
        Assert.assertEquals(ex2.getMessage(), "foo");
        Assert.assertEquals(ex2.getCause(), ex);
    }
}
