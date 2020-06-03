/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.NoDelay;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link org.btrplace.json.model.constraint.NoDelayConverter}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.json.model.constraint.NoDelayConverter
 */
public class NoDelayConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        Model mo = new DefaultModel();
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new NoDelayConverter());


        NoDelay nd = new NoDelay(mo.newVM());
        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(nd)), nd);
        System.out.println(conv.toJSON(nd));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(NoDelay.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new NoDelayConverter().getJSONId()));
    }

}
