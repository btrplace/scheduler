/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
        nsc.setModel(mo);
        JSONObject o = nsc.toJSON(ns);
        System.out.println(o);
        NamingService<VM> ns2 = nsc.fromJSON(o.toJSONString());
        Assert.assertEquals(ns, ns2);
    }
}
