/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.SatConstraint;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class DiscreteViolationExceptionTest {

    @Test
    public void test() {
        SatConstraint c = Mockito.mock(SatConstraint.class);
        Model m = new DefaultModel();
        DiscreteViolationException ex = new DiscreteViolationException(c, m);
        Assert.assertEquals(ex.getModel(), m);
        Assert.assertEquals(ex.getConstraint(), c);
        Assert.assertFalse(ex.toString().contains("null"));
    }
}