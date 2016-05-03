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
import org.btrplace.model.constraint.Constraint;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ConstraintConverter}.
 *
 * @author Fabien Hermenier
 */
public class ConstraintConverterTest {

    private class Mock<E extends Constraint> extends ConstraintConverter<E> {

        private String id;

        Mock(String i) {
            id = i;
        }

        @Override
        public Class<E> getSupportedConstraint() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getJSONId() {
            return id;
        }

        @Override
        public E fromJSON(Model mo, JSONObject in) throws JSONConverterException {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONObject toJSON(E e) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void testCheckId() throws JSONConverterException {
        ConstraintConverter<?> c = new Mock<>("foo");
        JSONObject o = new JSONObject();
        o.put("id", "foo");
        c.checkId(o);
    }

    @Test(expectedExceptions = {JSONConverterException.class})
    public void testBadCheckIdNoId() throws JSONConverterException {
        ConstraintConverter<?> c = new Mock<>("foo");
        JSONObject o = new JSONObject();
        c.checkId(o);
    }

    @Test(expectedExceptions = {JSONConverterException.class})
    public void testBadCheckIdBadId() throws JSONConverterException {
        ConstraintConverter<?> c = new Mock<>("foo");
        JSONObject o = new JSONObject();
        o.put("id", "bar");
        c.checkId(o);
    }

}
