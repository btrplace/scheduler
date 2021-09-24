/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.view;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link org.btrplace.json.model.view.ShareableResourceConverter}.
 *
 * @author Fabien Hermenier
 */
public class ShareableResourceConverterTest {

    @Test
    public void testSimple() throws JSONConverterException {
        Model mo = new DefaultModel();
        ShareableResource rc = new ShareableResource("foo", 3, 8);
        rc.setConsumption(mo.newVM(), 3);
        rc.setConsumption(mo.newVM(), 4);
        rc.setCapacity(mo.newNode(), 5);
        rc.setCapacity(mo.newNode(), 6);
        ShareableResourceConverter s = new ShareableResourceConverter();
        ShareableResource rc2 = s.fromJSON(mo, s.toJSON(rc));
        Assert.assertEquals(rc, rc2);
    }

    @Test(dependsOnMethods = {"testSimple"})
    public void testWithDifferentRcId() throws JSONConverterException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        ShareableResourceConverter s = new ShareableResourceConverter();
        ShareableResource rc = new ShareableResource("foo");
        rc.setConsumption(vm1, 3).setConsumption(vm2, 4).setCapacity(n1, 5);
        ShareableResource rcBis = s.fromJSON(mo, s.toJSON(rc));

        ShareableResource rc2 = new ShareableResource("bar");
        rc2.setConsumption(vm1, 3).setConsumption(vm2, 4).setCapacity(n1, 5);

        ShareableResource rc2Bis = s.fromJSON(mo, s.toJSON(rc2));
        Assert.assertFalse(rcBis.equals(rc2Bis));

    }
}
