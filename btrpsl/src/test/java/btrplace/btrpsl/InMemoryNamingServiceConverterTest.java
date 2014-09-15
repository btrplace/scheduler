/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.btrpsl;

import btrplace.json.JSONConverterException;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import net.minidev.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Unit tests for {@link InMemoryNamingServiceConverter}.
 *
 * @author Fabien Hermenier
 */
public class InMemoryNamingServiceConverterTest {

    @Test
    public void test1() throws NamingServiceException, JSONConverterException, IOException {
        InMemoryNamingService ns = new InMemoryNamingService();
        Model mo = new DefaultModel();
        ns.register("foo.VM1", mo.newVM());
        ns.register("bar.VM2", mo.newVM());
        ns.register("@N1", mo.newNode());
        ns.register("@N2", mo.newNode());
        InMemoryNamingServiceConverter conv = new InMemoryNamingServiceConverter();
        conv.setModel(mo);
        JSONObject o = conv.toJSON(ns);
        System.out.println(o);
        NamingService ns2 = conv.fromJSON(o.toJSONString());
        Assert.assertEquals(ns, ns2);
    }
}
