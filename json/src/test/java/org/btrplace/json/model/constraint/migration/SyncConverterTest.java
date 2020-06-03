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
import org.btrplace.model.constraint.migration.Sync;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link org.btrplace.json.model.constraint.migration.SyncConverter}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.json.model.constraint.migration.SyncConverter
 */
public class SyncConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        Model mo = new DefaultModel();
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new SyncConverter());


        Sync sync = new Sync(mo.newVM(), mo.newVM());
        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(sync)).toString(), sync.toString());
        System.out.println(conv.toJSON(sync));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(Sync.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new SyncConverter().getJSONId()));
    }

}
