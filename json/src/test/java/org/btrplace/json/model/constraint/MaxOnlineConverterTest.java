/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.MaxOnline;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link MaxOnline}.
 *
 * @author TU HUYNH DANG
 */
public class MaxOnlineConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        Model model = new DefaultModel();
        Set<Node> s = new HashSet<>(Arrays.asList(model.newNode(), model.newNode(), model.newNode()));
        MaxOnline mo = new MaxOnline(s, 2);
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new MaxOnlineConverter());


        Constraint new_max = conv.fromJSON(model, conv.toJSON(mo));
        Assert.assertEquals(mo, new_max);
        System.out.println(conv.toJSON(mo));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(MaxOnline.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new MaxOnlineConverter().getJSONId()));
    }

}
