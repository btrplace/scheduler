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

package btrplace.model;

import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.UUID;

/**
 * Basic tests for {@link SatConstraint}.
 *
 * @author Fabien Hermenier
 */
public class SatConstraintTest {

    static class MockSatConstraints extends SatConstraint {

        public MockSatConstraints() {
            super(new HashSet<UUID>(), new HashSet<UUID>());
            getInvolvedVMs().add(UUID.randomUUID());
        }

        @Override
        public Sat isSatisfied(Model i) {
            return Sat.UNDEFINED;
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
