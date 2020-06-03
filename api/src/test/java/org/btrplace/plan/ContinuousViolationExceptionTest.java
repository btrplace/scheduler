/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.event.Action;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class ContinuousViolationExceptionTest {

    @Test
    public void test() {
        SatConstraint c = Mockito.mock(SatConstraint.class);
        Action a = Mockito.mock(Action.class);
        ContinuousViolationException ex = new ContinuousViolationException(c, a);
        Assert.assertEquals(ex.getAction(), a);
        Assert.assertEquals(ex.getConstraint(), c);
        Assert.assertFalse(ex.toString().contains("null"));
    }

}
