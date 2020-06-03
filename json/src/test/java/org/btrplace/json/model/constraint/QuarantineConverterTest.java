/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Quarantine;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link org.btrplace.json.model.constraint.QuarantineConverter}.
 *
 * @author Fabien Hermenier
 */
public class QuarantineConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        Model mo = new DefaultModel();
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new QuarantineConverter());


        Quarantine d = new Quarantine(mo.newNode());

        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(d)), d);
        System.out.println(conv.toJSON(d));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(Quarantine.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new QuarantineConverter().getJSONId()));
    }

}
