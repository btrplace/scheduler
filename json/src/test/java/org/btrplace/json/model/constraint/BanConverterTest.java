/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Ban;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Unit tests for {@link org.btrplace.json.model.constraint.BanConverter}.
 *
 * @author Fabien Hermenier
 */
public class BanConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new BanConverter());
        Model mo = new DefaultModel();

        Ban d = new Ban(mo.newVM(),
                Arrays.asList(mo.newNode(), mo.newNode()));

        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(d)), d);
        System.out.println(conv.toJSON(d));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(Ban.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new BanConverter().getJSONId()));
    }

}
