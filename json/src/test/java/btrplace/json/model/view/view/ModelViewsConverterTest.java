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

package btrplace.json.model.view.view;

import btrplace.json.JSONConverterException;
import btrplace.json.model.view.ModelViewConverter;
import btrplace.json.model.view.ModelViewsConverter;
import btrplace.model.view.ModelView;
import junit.framework.Assert;
import net.minidev.json.JSONObject;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link btrplace.json.model.view.ModelViewsConverter}.
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
        public ModelView clone() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean substitute(UUID curId, UUID nextId) {
            throw new UnsupportedOperationException();
        }
    }

    public static class MockModelViewConverter extends ModelViewConverter<MockModelView> {

        @Override
        public Class<MockModelView> getSupportedConstraint() {
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
        public MockModelView fromJSON(JSONObject in) throws JSONConverterException {
            return new MockModelView(in.get("value").toString());
        }
    }

    @Test
    public void testRegister() {
        ModelViewsConverter c = new ModelViewsConverter();
        Assert.assertNull(c.register(new MockModelViewConverter()));
        Assert.assertTrue(c.getSupportedJavaViews().contains(MockModelView.class));
        Assert.assertTrue(c.getSupportedJSONViews().contains("mockView"));
    }

    @Test(dependsOnMethods = {"testRegister"})
    public void testWithExistingConverter() throws JSONConverterException {
        ModelViewsConverter c = new ModelViewsConverter();
        Assert.assertNull(c.register(new MockModelViewConverter()));
        MockModelView m = new MockModelView("bar");
        JSONObject o = c.toJSON(m);
        MockModelView m2 = (MockModelView) c.fromJSON(o);
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
        c.fromJSON(ob);
    }

    @Test(dependsOnMethods = {"testRegister"}, expectedExceptions = {JSONConverterException.class})
    public void testFromJSONWithoutID() throws JSONConverterException {
        JSONObject ob = new JSONObject();
        ob.put("value", "val");
        ModelViewsConverter c = new ModelViewsConverter();
        Assert.assertNull(c.register(new MockModelViewConverter()));
        c.fromJSON(ob);
    }
}
