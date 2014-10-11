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

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Unit tests for {@link ConstraintsConverter}.
 *
 * @author Fabien Hermenier
 */
public class ConstraintsConverterTest {

    public static class MockSatConstraint extends SatConstraint {

        String str;

        public MockSatConstraint(String s) {
            super(Collections.<VM>emptySet(), Collections.<Node>emptySet(), true);
            str = s;

        }

        @Override
        public boolean isSatisfied(Model i) {
            return false;
        }
    }

    public static class MockConstraintConverter extends ConstraintConverter<MockSatConstraint> {

        @Override
        public Class getSupportedConstraint() {
            return MockSatConstraint.class;
        }

        @Override
        public String getJSONId() {
            return "mock";
        }

        @Override
        public MockSatConstraint fromJSON(JSONObject in) throws JSONConverterException {
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
        MockSatConstraint m2 = (MockSatConstraint) c.fromJSON(o);
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
        c.fromJSON(ob);
    }

    @Test(dependsOnMethods = {"testRegister"}, expectedExceptions = {JSONConverterException.class})
    public void testFromJSONWithoutID() throws JSONConverterException {
        JSONObject ob = new JSONObject();
        ob.put("value", "val");
        ConstraintsConverter c = new ConstraintsConverter();
        Assert.assertNull(c.register(new MockConstraintConverter()));
        c.fromJSON(ob);
    }

    @Test
    public void testWithMultipleViews() throws JSONConverterException, IOException {
        ConstraintsConverter c = new ConstraintsConverter();
        org.testng.Assert.assertNull(c.register(new MockConstraintConverter()));
        List<SatConstraint> l = new ArrayList<>();
        l.add(new MockSatConstraint("foo"));
        l.add(new MockSatConstraint("bar"));
        String o = c.toJSONString(l);
        List<SatConstraint> l2 = c.listFromJSON(o);
        org.testng.Assert.assertEquals(l2.size(), l.size());
        int j = 0;
        for (int i = 0; i < l2.size(); i++) {
            MockSatConstraint v = (MockSatConstraint) l2.get(i);
            if (v.str.equals("foo")) {
                j++;
            } else if (v.str.equals("bar")) {
                j--;
            } else {
                org.testng.Assert.fail("Unexpected identifier: " + v.str);
            }
        }
        org.testng.Assert.assertEquals(j, 0);
    }
}
