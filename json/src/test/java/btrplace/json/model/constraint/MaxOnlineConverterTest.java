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

package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.constraint.MaxOnline;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link MaxOnline}.
 * User: TU HUYNH DANG
 * Date: 5/24/13
 * Time: 9:22 AM
 */
public class MaxOnlineConverterTest {

    @Test
    public void testViables() throws JSONConverterException, IOException {
        Model model = new DefaultModel();
        Set<Node> s = new HashSet<Node>(Arrays.asList(model.newNode(), model.newNode(), model.newNode()));
        MaxOnline mo = new MaxOnline(s, 2);
        MaxOnlineConverter moc = new MaxOnlineConverter();
        moc.setModel(model);
        MaxOnline new_max = moc.fromJSON(moc.toJSONString(mo));
        Assert.assertEquals(mo, new_max);
    }
}
