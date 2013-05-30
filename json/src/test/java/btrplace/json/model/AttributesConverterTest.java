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

package btrplace.json.model;

import btrplace.json.JSONConverterException;
import btrplace.model.Attributes;
import btrplace.model.DefaultAttributes;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Unit tests for {@link btrplace.json.model.AttributesConverter}.
 *
 * @author Fabien Hermenier
 */
public class AttributesConverterTest {

    @Test
    public void testSimple() throws IOException, JSONConverterException {
        Model mo = new DefaultModel();
        Attributes attrs = new DefaultAttributes();

        attrs.put(mo.newVM(), "foo", true);
        attrs.put(mo.newNode(), "foo", false);
        attrs.put(mo.newVM(), "bar", 5);
        attrs.put(mo.newVM(), "baz", "zab");
        attrs.put(mo.newNode(), "ba", 1.34);

        AttributesConverter json = new AttributesConverter();
        json.setModel(mo);
        String o = json.toJSONString(attrs);
        Attributes attrs2 = json.fromJSON(o);
        Assert.assertTrue(attrs.equals(attrs2));
    }
}
