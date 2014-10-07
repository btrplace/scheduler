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

package org.btrplace.json.model;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Unit tests for {@link org.btrplace.json.model.AttributesConverter}.
 *
 * @author Fabien Hermenier
 */
public class AttributesConverterTest {

    @Test
    public void testSimple() throws IOException, JSONConverterException {
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

        AttributesConverter json = new AttributesConverter();
        json.setModel(mo);
        String o = json.toJSONString(attrs);
        System.out.println(o);
        Attributes attrs2 = json.fromJSON(o);
        Assert.assertTrue(attrs.equals(attrs2));
    }
}
