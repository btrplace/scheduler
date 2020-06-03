/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.view;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.view.NamingService;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link org.btrplace.json.model.view.NamingServiceConverter}.
 *
 * @author Fabien Hermenier
 */
public class NamingServiceConverterTest {

    @Test
    public void test() throws JSONConverterException {
        NamingService<VM> ns = NamingService.newVMNS();
        Model mo = new DefaultModel();
        for (int i = 0; i < 10; i++) {
            VM v = mo.newVM();
            ns.register(v, "VM " + i);
        }
        NamingServiceConverter nsc = new NamingServiceConverter();
        JSONObject o = nsc.toJSON(ns);
        System.out.println(o);
        @SuppressWarnings("unchecked")
        NamingService<VM> ns2 = (NamingService<VM>) nsc.fromJSON(mo, o);
        Assert.assertEquals(ns, ns2);
    }
}
