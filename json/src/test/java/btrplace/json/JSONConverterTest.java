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

package btrplace.json;

import junit.framework.Assert;
import net.minidev.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link JSONConverter}.
 *
 * @author Fabien Hermenier
 */
public class JSONConverterTest {

    @Test
    public void testValidRequiredUUID() throws JSONConverterException {
        JSONObject o = new JSONObject();
        UUID u = UUID.randomUUID();
        o.put("id", u);
        Assert.assertEquals(JSONConverter.requiredUUID(o, "id"), u);
    }

    @DataProvider(name = "getInvalidUUIDs")
    public Object[][] getInvalidUUIDs() {
        return new Object[][]{
                {"id", "id", "toto"}, //bad type
                {"id", "foo", UUID.randomUUID()} //bad write key
        };
    }

    @Test(expectedExceptions = {JSONConverterException.class}, dataProvider = "getInvalidUUIDs")
    public void testInValidRequiredUUID(String storeKey, String readKey, Object o) throws JSONConverterException {
        JSONObject obj = new JSONObject();
        obj.put(storeKey, o);
        JSONConverter.requiredUUID(obj, readKey);
    }

    @Test
    public void testValidRequiredString() throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("id", "bar");
        Assert.assertEquals(JSONConverter.requiredString(o, "id"), "bar");
    }

    @Test(expectedExceptions = {JSONConverterException.class})
    public void testInValidRequiredString() throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("id", "bar");
        JSONConverter.requiredString(o, "bar");
    }

    @Test
    public void testValidRequiredDouble() throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("id", 1.3d);
        Assert.assertEquals(JSONConverter.requiredDouble(o, "id"), 1.3d);

        o.put("id", 145);
        Assert.assertEquals(JSONConverter.requiredDouble(o, "id"), 145d);
    }

    @DataProvider(name = "getInvalidDoubles")
    public Object[][] getInvalidDoubles() {
        return new Object[][]{
                {"id", "id", "toto"}, //bad type
                {"id", "foo", 1234d} //bad key
        };
    }

    @Test(expectedExceptions = {JSONConverterException.class}, dataProvider = "getInvalidDoubles")
    public void testInValidRequiredDoubles(String storeKey, String readKey, Object o) throws JSONConverterException {
        JSONObject obj = new JSONObject();
        obj.put(storeKey, o);
        JSONConverter.requiredDouble(obj, readKey);
    }

    @Test
    public void testValidRequiredLong() throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("id", 123553l);
        Assert.assertEquals(JSONConverter.requiredLong(o, "id"), 123553l);
    }

    @DataProvider(name = "getInvalidLongs")
    public Object[][] getInvalidLongs() {
        return new Object[][]{
                {"id", "id", "toto"}, //bad type
                {"id", "id", 1234.5}, //bad type
                {"id", "id", false}, //bad type
                {"id", "foo", 1234l}, //bad key
        };
    }

    @Test(expectedExceptions = {JSONConverterException.class}, dataProvider = "getInvalidLongs")
    public void testInValidRequiredLongs(String storeKey, String readKey, Object o) throws JSONConverterException {
        JSONObject obj = new JSONObject();
        obj.put(storeKey, o);
        JSONConverter.requiredLong(obj, readKey);
    }
}
