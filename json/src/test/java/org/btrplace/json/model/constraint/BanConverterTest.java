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

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Ban;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Unit tests for {@link org.btrplace.json.model.constraint.BanConverter}.
 *
 * @author Fabien Hermenier
 */
public class BanConverterTest {

    @Test
    public void testViables() throws JSONConverterException, IOException {
        BanConverter conv = new BanConverter();
        Model mo = new DefaultModel();
        conv.setModel(mo);
        Ban d = new Ban(mo.newVM(),
                new HashSet<>(Arrays.asList(mo.newNode(), mo.newNode())));

        Assert.assertEquals(conv.fromJSON(conv.toJSONString(d)), d);
        System.out.println(conv.toJSONString(d));
    }
}
