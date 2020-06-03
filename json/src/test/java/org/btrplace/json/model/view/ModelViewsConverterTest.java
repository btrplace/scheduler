/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.view;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.view.ModelView;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link org.btrplace.json.model.view.ModelViewsConverter}.
 *
 * @author Fabien Hermenier
 */
public class ModelViewsConverterTest {

    public static class MockModelView implements ModelView {

        String value;

        public MockModelView(String v) {
            value = v;
        }

        @Override
        public String getIdentifier() {
            return "mock";
        }

        @Override
        public ModelView copy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean substituteVM(VM curId, VM nextId) {
            throw new UnsupportedOperationException();
        }
    }

    public static class MockModelViewConverter implements ModelViewConverter<MockModelView> {

        @Override
        public Class<MockModelView> getSupportedView() {
            return MockModelView.class;
        }

        @Override
        public String getJSONId() {
            return "mockView";
        }

        @Override
        public JSONObject toJSON(MockModelView view) {
            JSONObject o = new JSONObject();
            o.put("id", getJSONId());
            o.put("value", view.value);
            return o;
        }

        @Override
        public MockModelView fromJSON(Model mo, JSONObject in) throws JSONConverterException {
            return new MockModelView(in.get("value").toString());
        }
    }

    @Test
    public void testRegister() {
        ModelViewsConverter c = new ModelViewsConverter();
        c.register(new MockModelViewConverter());
        Assert.assertTrue(c.getSupportedJavaViews().contains(MockModelView.class));
        Assert.assertTrue(c.getSupportedJSONViews().contains("mockView"));
    }

    @Test(dependsOnMethods = {"testRegister"})
    public void testWithExistingConverter() throws JSONConverterException {
        ModelViewsConverter c = new ModelViewsConverter();
        c.register(new MockModelViewConverter());
        MockModelView m = new MockModelView("bar");
        JSONObject o = c.toJSON(m);
        MockModelView m2 = (MockModelView) c.fromJSON(null, o);
        Assert.assertEquals(m.value, m2.value);
    }

    @Test(dependsOnMethods = {"testRegister"}, expectedExceptions = {JSONConverterException.class})
    public void testToJSonWithNoConverters() throws JSONConverterException {
        ModelViewsConverter c = new ModelViewsConverter();
        MockModelView m = new MockModelView("bar");
        c.toJSON(m);
    }

    @Test(dependsOnMethods = {"testRegister"}, expectedExceptions = {JSONConverterException.class})
    public void testFromJSonWithNoConverter() throws JSONConverterException {
        JSONObject ob = new JSONObject();
        ob.put("id", "mockView");
        ob.put("value", "val");
        ModelViewsConverter c = new ModelViewsConverter();
        c.fromJSON(null, ob);
    }

    @Test(dependsOnMethods = {"testRegister"}, expectedExceptions = {JSONConverterException.class})
    public void testFromJSONWithoutID() throws JSONConverterException {
        JSONObject ob = new JSONObject();
        ob.put("value", "val");
        ModelViewsConverter c = new ModelViewsConverter();
        c.register(new MockModelViewConverter());
        c.fromJSON(null, ob);
    }
}
