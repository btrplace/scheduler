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

package org.btrplace.model;

import org.btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;

/**
 * Basic tests for {@link org.btrplace.model.constraint.SatConstraint}.
 *
 * @author Fabien Hermenier
 */
public class SatConstraintTest {

    static class MockSatConstraints extends SatConstraint {

        public MockSatConstraints() {
            super(new HashSet<VM>(), new HashSet<Node>(), false);
            getInvolvedVMs().add(new VM(5));
        }

        @Override
        public boolean isSatisfied(Model i) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void testGetters() {
        SatConstraint c = new MockSatConstraints();
        Assert.assertEquals(1, c.getInvolvedVMs().size());
        Assert.assertTrue(c.getInvolvedNodes().isEmpty());
        Assert.assertTrue(c.setContinuous(true));
        Assert.assertTrue(c.isContinuous());
        Assert.assertTrue(c.setContinuous(false));
        Assert.assertFalse(c.isContinuous());
    }
}
