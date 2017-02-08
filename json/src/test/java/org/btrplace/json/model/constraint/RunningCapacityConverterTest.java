/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.RunningCapacity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Unit tests for {@link org.btrplace.model.constraint.RunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class RunningCapacityConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new RunningCapacityConverter());

        Model mo = new DefaultModel();

        RunningCapacity d = new RunningCapacity(new HashSet<>(Arrays.asList(mo.newNode(), mo.newNode(), mo.newNode())), 5, false);
        RunningCapacity c = new RunningCapacity(new HashSet<>(Arrays.asList(mo.newNode(), mo.newNode())), 5, true);

        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(d)), d);
        Assert.assertEquals(conv.fromJSON(mo, conv.toJSON(c)), c);
        System.out.println(conv.toJSON(d));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(RunningCapacity.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new RunningCapacityConverter().getJSONId()));
    }

}
