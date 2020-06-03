/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Seq;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Unit tests for {@link org.btrplace.model.constraint.Seq}.
 *
 * @author Fabien Hermenier
 */
public class SeqConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        Model mo = new DefaultModel();
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new SeqConverter());


        Seq d = new Seq(Arrays.asList(mo.newVM(), mo.newVM(), mo.newVM()));
        Seq c = new Seq(Arrays.asList(mo.newVM(), mo.newVM(), mo.newVM()));

        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(d)), d);
        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(c)), c);
        System.out.println(conv.toJSON(d));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(Seq.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new SeqConverter().getJSONId()));
    }

}
