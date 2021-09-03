/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.RunningCapacity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Unit tests for {@link org.btrplace.model.constraint.RunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class RunningCapacityConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new RunningCapacityConverter());

        Model mo = new DefaultModel();

        RunningCapacity d = new RunningCapacity(new HashSet<>(Arrays.asList(mo.newNode(), mo.newNode(), mo.newNode())), 5);
        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(d)), d);
        System.out.println(conv.toJSON(d));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(RunningCapacity.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new RunningCapacityConverter().getJSONId()));
    }

}
