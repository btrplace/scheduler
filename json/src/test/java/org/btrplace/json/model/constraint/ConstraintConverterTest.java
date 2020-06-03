/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

    private class Mock<E extends Constraint> implements ConstraintConverter<E> {

        private final String id;

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
