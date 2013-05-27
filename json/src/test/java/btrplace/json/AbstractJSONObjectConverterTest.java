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

package btrplace.json;

import junit.framework.Assert;
import net.minidev.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Random;


/**
 * Unit tests for {@link AbstractJSONObjectConverter}.
 *
 * @author Fabien Hermenier
 */
public class AbstractJSONObjectConverterTest {

    private static Random rnd = new Random();

    @Test
    public void testValidRequiredint() throws JSONConverterException {
        JSONObject o = new JSONObject();
        int u = rnd.nextInt();
        o.put("id", u);
        Assert.assertEquals(AbstractJSONObjectConverter.requiredInt(o, "id"), u);
    }

    @DataProvider(name = "getInvalidints")
    public Object[][] getInvalidints() {
        return new Object[][]{
                {"id", "id", "toto"}, //bad type
                {"id", "foo", rnd.nextInt()} //bad write key
        };
    }

    @Test(expectedExceptions = {JSONConverterException.class}, dataProvider = "getInvalidints")
    public void testInValidRequiredint(String storeKey, String readKey, Object o) throws JSONConverterException {
        JSONObject obj = new JSONObject();
        obj.put(storeKey, o);
        AbstractJSONObjectConverter.requiredInt(obj, readKey);
    }

    @Test
    public void testValidRequiredString() throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("id", "bar");
        Assert.assertEquals(AbstractJSONObjectConverter.requiredString(o, "id"), "bar");
    }

    @Test(expectedExceptions = {JSONConverterException.class})
    public void testInValidRequiredString() throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("id", "bar");
        AbstractJSONObjectConverter.requiredString(o, "bar");
    }

    @Test
    public void testValidRequiredDouble() throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("id", 1.3d);
        Assert.assertEquals(AbstractJSONObjectConverter.requiredDouble(o, "id"), 1.3d);

        o.put("id", 145);
        Assert.assertEquals(AbstractJSONObjectConverter.requiredDouble(o, "id"), 145d);
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
        AbstractJSONObjectConverter.requiredDouble(obj, readKey);
    }

    @Test
    public void testValidRequiredLong() throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("id", 123553l);
        Assert.assertEquals(AbstractJSONObjectConverter.requiredLong(o, "id"), 123553l);
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
        AbstractJSONObjectConverter.requiredLong(obj, readKey);
    }
}
