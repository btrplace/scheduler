/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Lonely;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Unit tests for {@link org.btrplace.json.model.constraint.LonelyConverter}.
 *
 * @author Fabien Hermenier
 */
public class LonelyConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        Model mo = new DefaultModel();
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new LonelyConverter());


        Lonely d = new Lonely(new HashSet<>(Arrays.asList(mo.newVM(), mo.newVM())), false);
        Lonely c = new Lonely(new HashSet<>(Arrays.asList(mo.newVM(), mo.newVM())), true);

        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(d)), d);
        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(c)), c);
        System.out.println(conv.toJSON(d));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(Lonely.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new LonelyConverter().getJSONId()));
    }

}
