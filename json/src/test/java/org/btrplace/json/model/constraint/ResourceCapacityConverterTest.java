/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.ResourceCapacity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Unit tests for {@link org.btrplace.model.constraint.ResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class ResourceCapacityConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new ResourceCapacityConverter());

        Model mo = new DefaultModel();

        ResourceCapacity d = new ResourceCapacity(new HashSet<>(Arrays.asList(mo.newNode(), mo.newNode(), mo.newNode())), "cpu", 5, false);
        ResourceCapacity c = new ResourceCapacity(new HashSet<>(Arrays.asList(mo.newNode(), mo.newNode())), "mem", 5, true);

        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(d)), d);
        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(c)), c);
        System.out.println(conv.toJSON(d));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(ResourceCapacity.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new ResourceCapacityConverter().getJSONId()));
    }

}
