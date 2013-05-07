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

package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.constraint.Overbook;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Unit tests for {@link btrplace.json.model.constraint.OverbookConverter}.
 *
 * @author Fabien Hermenier
 */
public class OverbookConverterTest implements PremadeElements {

    private static OverbookConverter conv = new OverbookConverter();

    @Test
    public void testViables() throws JSONConverterException {
        Overbook d = new Overbook(new HashSet<>(Arrays.asList(n1, n2, n3)), "foo", 1.4);
        Assert.assertEquals(conv.fromJSON(conv.toJSON(d)), d);
    }
}
