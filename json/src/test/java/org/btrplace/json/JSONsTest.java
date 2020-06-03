/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json;

import net.minidev.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Random;


/**
 * Unit tests for {@link JSONs}.
 *
 * @author Fabien Hermenier
 */
public class JSONsTest {

  private static final Random rnd = new Random();

    @Test
    public void testValidRequiredInt() throws JSONConverterException {
        JSONObject o = new JSONObject();
        int u = rnd.nextInt();
        o.put("id", u);
        Assert.assertEquals(JSONs.requiredInt(o, "id"), u);
    }

    @DataProvider(name = "getInvalidInts")
    public Object[][] getInvalidInts() {
        return new Object[][]{
                {"id", "id", "toto"}, //bad type
                {"id", "foo", rnd.nextInt()} //bad write key
        };
    }

    @Test(expectedExceptions = {JSONConverterException.class}, dataProvider = "getInvalidInts")
    public void testInValidRequiredInt(String storeKey, String readKey, Object o) throws JSONConverterException {
        JSONObject obj = new JSONObject();
        obj.put(storeKey, o);
        JSONs.requiredInt(obj, readKey);
    }

    @Test
    public void testValidRequiredString() throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("id", "bar");
        Assert.assertEquals(JSONs.requiredString(o, "id"), "bar");
    }

    @Test(expectedExceptions = {JSONConverterException.class})
    public void testInValidRequiredString() throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("id", "bar");
        JSONs.requiredString(o, "bar");
    }

    @Test
    public void testValidRequiredDouble() throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("id", 1.3d);
        Assert.assertEquals(JSONs.requiredDouble(o, "id"), 1.3d);

        o.put("id", 145);
        Assert.assertEquals(JSONs.requiredDouble(o, "id"), 145d);
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
        JSONs.requiredDouble(obj, readKey);
    }
}
