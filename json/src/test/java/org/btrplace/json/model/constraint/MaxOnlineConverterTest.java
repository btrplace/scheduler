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
import org.btrplace.model.Node;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.MaxOnline;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link MaxOnline}.
 *
 * @author TU HUYNH DANG
 */
public class MaxOnlineConverterTest {

    @Test
    public void testViables() throws JSONConverterException {
        Model model = new DefaultModel();
        Set<Node> s = new HashSet<>(Arrays.asList(model.newNode(), model.newNode(), model.newNode()));
        MaxOnline mo = new MaxOnline(s, 2);
        ConstraintsConverter conv = new ConstraintsConverter();
        conv.register(new MaxOnlineConverter());


        Constraint new_max = conv.fromJSON(model, conv.toJSON(mo));
        Assert.assertEquals(mo, new_max);
        System.out.println(conv.toJSON(mo));
    }

    @Test
    public void testBundle() {
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJavaConstraints().contains(MaxOnline.class));
        Assert.assertTrue(ConstraintsConverter.newBundle().getSupportedJSONConstraints().contains(new MaxOnlineConverter().getJSONId()));
    }

}
