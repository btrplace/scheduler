/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;


/**
 * Unit tests for {@link ConstraintsConverter}.
 *
 * @author Fabien Hermenier
 */
public class ConstraintsConverterTest {

    public static class MockSatConstraint implements SatConstraint {

        String str;

        public MockSatConstraint(String s) {
            str = s;
        }

        @Override
        public Collection<Node> getInvolvedNodes() {
            return null;
        }

        @Override
        public Collection<VM> getInvolvedVMs() {
            return null;
        }

        @Override
        public boolean isContinuous() {
            return false;
        }

        @Override
        public boolean setContinuous(boolean b) {
            return false;
        }
    }

    public static class MockConstraintConverter implements ConstraintConverter<MockSatConstraint> {

        @Override
        public Class<MockSatConstraint> getSupportedConstraint() {
            return MockSatConstraint.class;
        }

        @Override
        public String getJSONId() {
            return "mock";
        }

        @Override
        public MockSatConstraint fromJSON(Model mo, JSONObject in) throws JSONConverterException {
            return new MockSatConstraint(in.get("value").toString());
        }

        @Override
        public JSONObject toJSON(MockSatConstraint o) {
            JSONObject j = new JSONObject();
            j.put("id", getJSONId());
            j.put("value", o.str);
            return j;
        }
    }

    @Test
    public void testRegister() {
        ConstraintsConverter c = new ConstraintsConverter();
        Assert.assertNull(c.register(new MockConstraintConverter()));
        Assert.assertTrue(c.getSupportedJavaConstraints().contains(MockSatConstraint.class));
        Assert.assertTrue(c.getSupportedJSONConstraints().contains("mock"));
    }

    @Test(dependsOnMethods = {"testRegister"})
    public void testWithExistingConverter() throws JSONConverterException {
        ConstraintsConverter c = new ConstraintsConverter();
        Assert.assertNull(c.register(new MockConstraintConverter()));
        MockSatConstraint m = new MockSatConstraint("bar");
        JSONObject o = c.toJSON(m);
        MockSatConstraint m2 = (MockSatConstraint) c.fromJSON(null, o);
        Assert.assertEquals(m.str, m2.str);
    }

    @Test(dependsOnMethods = {"testRegister"}, expectedExceptions = {JSONConverterException.class})
    public void testToJSonWithNoConverters() throws JSONConverterException {
        ConstraintsConverter c = new ConstraintsConverter();
        MockSatConstraint m = new MockSatConstraint("bar");
        c.toJSON(m);
    }

    @Test(dependsOnMethods = {"testRegister"}, expectedExceptions = {JSONConverterException.class})
    public void testFromJSonWithNoConverter() throws JSONConverterException {
        JSONObject ob = new JSONObject();
        ob.put("id", "mock");
        ob.put("value", "val");
        ConstraintsConverter c = new ConstraintsConverter();
        c.fromJSON(null, ob);
    }

    @Test(dependsOnMethods = {"testRegister"}, expectedExceptions = {JSONConverterException.class})
    public void testFromJSONWithoutID() throws JSONConverterException {
        JSONObject ob = new JSONObject();
        ob.put("value", "val");
        ConstraintsConverter c = new ConstraintsConverter();
        Assert.assertNull(c.register(new MockConstraintConverter()));
        c.fromJSON(null, ob);
    }
}
