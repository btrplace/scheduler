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
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Split;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Unit tests for {@link org.btrplace.json.model.constraint.SplitConverter}.
 *
 * @author Fabien Hermenier
 */
public class SplitConverterTest {

    @Test
    public void testViables() throws JSONConverterException, IOException {
        Model mo = new DefaultModel();

        Collection<VM> s1 = new HashSet<>(Arrays.asList(mo.newVM(), mo.newVM()));
        Collection<VM> s2 = new HashSet<>(Arrays.asList(mo.newVM(), mo.newVM()));
        Collection<VM> s3 = new HashSet<>(Arrays.asList(mo.newVM()));
        Set<Collection<VM>> vgrps = new HashSet<>(Arrays.asList(s1, s2, s3));
        Split d = new Split(vgrps, false);
        Split c = new Split(vgrps, true);

        SplitConverter conv = new SplitConverter();
        conv.setModel(mo);

        Assert.assertEquals(conv.fromJSON(conv.toJSONString(d)), d);
        Assert.assertEquals(conv.fromJSON(conv.toJSONString(c)), c);
        System.out.println(conv.toJSONString(d));
    }
}
