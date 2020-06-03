/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.view.ModelViewsConverter;
import org.btrplace.model.Attributes;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ModelConverterTest}.
 *
 * @author Fabien Hermenier
 */
public class ModelConverterTest {

    @Test
    public void testInstantiation() {
        ModelConverter conv = new ModelConverter();
        Assert.assertNotNull(conv.getViewsConverter());
        ModelViewsConverter vc = new ModelViewsConverter();
        conv.setModelViewConverters(vc);
        Assert.assertEquals(conv.getViewsConverter(), vc);
    }

    @Test
    public void testConversion() throws JSONConverterException {
        ModelConverter conv = new ModelConverter();
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        Node n1 = mo.newNode();
        VM vm1 = mo.newVM();
        m.addOnlineNode(n1);
        m.addReadyVM(vm1);
        Attributes attrs = mo.getAttributes();
        attrs.put(vm1, "boot", 5);
        attrs.put(n1, "type", "xen");

        ShareableResource rc = new ShareableResource("cpu");
        rc.setConsumption(vm1, 5);
        rc.setCapacity(n1, 10);
        mo.attach(rc);

        String jo = conv.toJSONString(mo);
        System.out.println(jo);
        Model res = conv.fromJSON(jo);
        Assert.assertEquals(res, mo);
        Assert.assertTrue(res.contains(n1));
        Assert.assertTrue(res.contains(vm1));
    }

    @Test
    public void testCompleteMapping() throws JSONConverterException {
        Model mo = new DefaultModel();
        Mapping c = mo.getMapping();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        c.addOnlineNode(n1);
        c.addOfflineNode(n2);
        c.addRunningVM(vm1, n1);
        c.addSleepingVM(vm2, n1);
        c.addReadyVM(vm3);
        c.addOnlineNode(n3);
        c.addRunningVM(vm4, n3);
        ModelConverter conv = new ModelConverter();
        JSONObject o = conv.toJSON(mo);
        System.out.println(o);
        Assert.assertEquals(conv.fromJSON(o), mo);
    }
}
