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
import org.btrplace.model.constraint.SplitAmong;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;


/**
 * Unit tests for {@link org.btrplace.json.model.constraint.SplitAmongConverter}.
 *
 * @author Fabien Hermenier
 */
public class SplitAmongConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        Model mo = new DefaultModel();
        Collection<VM> s1 = Arrays.asList(mo.newVM(), mo.newVM(), mo.newVM());
        Collection<VM> s2 = Arrays.asList(mo.newVM(), mo.newVM(), mo.newVM());
        Collection<VM> s3 = Arrays.asList(mo.newVM(), mo.newVM());
        Collection<Collection<VM>> vgrps = new HashSet<>(Arrays.asList(s1, s2, s3));

        Collection<Node> p1 = Arrays.asList(mo.newNode(), mo.newNode());
        Collection<Node> p2 = Arrays.asList(mo.newNode(), mo.newNode());
        Collection<Node> p3 = Collections.singletonList(mo.newNode());
        Collection<Collection<Node>> pgrps = new HashSet<>(Arrays.asList(p1, p2, p3));

        SplitAmong d = new SplitAmong(vgrps, pgrps, false);
        SplitAmong c = new SplitAmong(vgrps, pgrps, true);
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new SplitAmongConverter());



        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(d)), d);
        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(c)), c);
        System.out.println(conv.toJSON(d));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(SplitAmong.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new SplitAmongConverter().getJSONId()));
    }

}
