/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

import btrplace.model.Attributes;
import btrplace.model.DefaultAttributes;
import btrplace.test.PremadeElements;
import net.minidev.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Unit tests for {@link btrplace.json.model.AttributesConverter}.
 *
 * @author Fabien Hermenier
 */
public class AttributesConverterTest implements PremadeElements {

    @Test
    public void testSimple() throws IOException {
        Attributes attrs = new DefaultAttributes();

        attrs.put(n1, "foo", true);
        attrs.put(n2, "foo", false);
        attrs.put(n1, "bar", 5);
        attrs.put(n2, "baz", "zab");
        attrs.put(n2, "ba", 1.34);

        AttributesConverter json = new AttributesConverter();
        JSONObject o = json.toJSON(attrs);
        Attributes attrs2 = json.fromJSON(o);
        Assert.assertTrue(attrs.equals(attrs2));
    }
}
