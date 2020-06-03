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
import org.btrplace.model.constraint.migration.Deadline;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link org.btrplace.json.model.constraint.migration.DeadlineConverter}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.json.model.constraint.migration.DeadlineConverter
 */
public class DeadlineConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        Model mo = new DefaultModel();
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new DeadlineConverter());


        Deadline d = new Deadline(mo.newVM(), "+00:00:15");
        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(d)), d);
        System.out.println(conv.toJSON(d));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(Deadline.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new DeadlineConverter().getJSONId()));
    }

}
