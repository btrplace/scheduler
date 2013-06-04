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

package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import net.minidev.json.JSONObject;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link SatConstraintConverter}.
 *
 * @author Fabien Hermenier
 */
public class SatConstraintConverterTest {

    private class Mock extends SatConstraintConverter {

        private String id;

        Mock(String i) {
            id = i;
        }

        @Override
        public Class getSupportedConstraint() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getJSONId() {
            return id;
        }

        @Override
        public Object fromJSON(JSONObject in) throws JSONConverterException {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONObject toJSON(Object o) throws JSONConverterException {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void testCheckId() throws JSONConverterException {
        SatConstraintConverter c = new Mock("foo");
        JSONObject o = new JSONObject();
        o.put("id", "foo");
        c.checkId(o);
    }

    @Test(expectedExceptions = {JSONConverterException.class})
    public void testBadCheckIdNoId() throws JSONConverterException {
        SatConstraintConverter c = new Mock("foo");
        JSONObject o = new JSONObject();
        c.checkId(o);
    }

    @Test(expectedExceptions = {JSONConverterException.class})
    public void testBadCheckIdBadId() throws JSONConverterException {
        SatConstraintConverter c = new Mock("foo");
        JSONObject o = new JSONObject();
        o.put("id", "bar");
        c.checkId(o);
    }

}
