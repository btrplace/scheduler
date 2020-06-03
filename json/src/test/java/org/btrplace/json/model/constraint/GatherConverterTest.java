/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Gather;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Unit tests for {@link org.btrplace.json.model.constraint.SpreadConverter}.
 *
 * @author Fabien Hermenier
 */
public class GatherConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        Model mo = new DefaultModel();
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new GatherConverter());


        Gather d = new Gather(Arrays.asList(mo.newVM(), mo.newVM()), false);
        Gather c = new Gather(Arrays.asList(mo.newVM(), mo.newVM()), true);

        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(d)), d);
        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(c)), c);
        System.out.println(conv.toJSON(d));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(Gather.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new GatherConverter().getJSONId()));
    }

}
