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

package btrplace.btrpsl.constraint;

import btrplace.model.constraint.SatConstraint;
import org.reflections.Reflections;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link DefaultConstraintsCatalog}.
 *
 * @author Fabien Hermenier
 */
public class DefaultConstraintsCatalogTest {

    /**
     * Check if there is a builder for each SatConstraint
     * bundled with btrplace-api.
     */
    @Test
    public void testCovering() throws Exception {
        DefaultConstraintsCatalog c = DefaultConstraintsCatalog.newBundle();
        Reflections reflections = new Reflections("btrplace.model");
        Set<Class<? extends SatConstraint>> impls = new HashSet<>();
        for (Class cstr : reflections.getSubTypesOf(SatConstraint.class)) {
            if (!Modifier.isAbstract(cstr.getModifiers())) {
                impls.add(cstr);
            }
        }
        Assert.assertEquals(c.getAvailableConstraints().size(), impls.size());
    }
}
