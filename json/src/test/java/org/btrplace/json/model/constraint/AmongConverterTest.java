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
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Among;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for {@link org.btrplace.json.model.constraint.AmongConverter}.
 *
 * @author Fabien Hermenier
 */
public class AmongConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new AmongConverter());
        Model mo = new DefaultModel();
        List<VM> s1 = Arrays.asList(mo.newVM(), mo.newVM(), mo.newVM());
        Collection<Node> p1 = Arrays.asList(mo.newNode(), mo.newNode());
        List<Node> p2 = Arrays.asList(mo.newNode(), mo.newNode());
        List<Node> p3 = Collections.singletonList(mo.newNode());

        Set<Collection<Node>> pgrps = new HashSet<>(Arrays.asList(p1, p2, p3));

        Among d = new Among(s1, pgrps, false);
        Among c = new Among(s1, pgrps, true);
        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(d)), d);
        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(c)), c);
        System.out.println(conv.toJSON(d));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(Among.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new AmongConverter().getJSONId()));
    }
}
