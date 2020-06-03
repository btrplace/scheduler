/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Attributes;
import org.btrplace.model.DefaultAttributes;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link org.btrplace.json.model.AttributesConverter}.
 *
 * @author Fabien Hermenier
 */
public class AttributesConverterTest {

    @Test
    public void testSimple() throws JSONConverterException {
        Model mo = new DefaultModel();
        Attributes attrs = new DefaultAttributes();

        VM vm1 = mo.newVM();
        VM vm3 = mo.newVM(3);

        Node n1 = mo.newNode();

        attrs.put(n1, "boot", 7);
        attrs.put(vm1, "template", "xen");
        attrs.put(vm1, "forge", 3);
        attrs.put(vm3, "template", "kvm");
        attrs.put(vm3, "clone", true);
        attrs.put(vm3, "foo", 1.3);

        JSONObject o = AttributesConverter.toJSON(attrs);
        System.out.println(o);
        Attributes attrs2 = AttributesConverter.fromJSON(mo, o);
        Assert.assertTrue(attrs.equals(attrs2));
    }
}
