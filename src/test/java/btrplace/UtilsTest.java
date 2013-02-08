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

package btrplace;

import junit.framework.Assert;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link Utils}.
 *
 * @author Fabien Hermenier
 */
public class UtilsTest {

    @Test
    public void testReadObject() throws ParseException, JSONConverterException {
        String json = "{\"foo\":\"fooV\",\"bar\":[1,2,3,4,5],\"bool\":false}";
        JSONObject o = Utils.readObject(json);
        Assert.assertTrue(o.get("foo") instanceof String);
        Assert.assertTrue(o.get("bool") instanceof Boolean);
        Assert.assertTrue(o.get("bar") instanceof JSONArray);
    }
}
