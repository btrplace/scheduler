/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint.migration;

import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.constraint.ConstraintsConverter;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.migration.Serialize;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link org.btrplace.json.model.constraint.migration.SerializeConverter}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.json.model.constraint.migration.SerializeConverter
 */
public class SerializeConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        Model mo = new DefaultModel();
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new SerializeConverter());


        Serialize serial = new Serialize(mo.newVM(), mo.newVM());
        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(serial)).toString(), serial.toString());
        System.out.println(conv.toJSON(serial));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(Serialize.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new SerializeConverter().getJSONId()));
    }

}
