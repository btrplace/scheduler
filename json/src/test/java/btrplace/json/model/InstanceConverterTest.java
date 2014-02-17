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

package btrplace.json.model;

import btrplace.json.JSONConverterException;
import btrplace.model.*;
import btrplace.model.constraint.MinMTTR;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link InstanceConverter}.
 *
 * @author Fabien Hermenier
 */
public class InstanceConverterTest {

    @Test
    public void testConversion() throws JSONConverterException, IOException {
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();
        Node n1 = mo.newNode();
        VM vm1 = mo.newVM();
        ma.addOnlineNode(n1);
        ma.addOfflineNode(n1);
        ma.addReadyVM(vm1);

        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Online(ma.getAllNodes()));
        cstrs.add(new Running(mo.newVM()));
        Instance i = new Instance(mo, cstrs, new MinMTTR());

        InstanceConverter conv = new InstanceConverter();
        String o = conv.toJSONString(i);
        System.out.println(o);
        Instance res = conv.fromJSON(o);
        Assert.assertEquals(i, res);
    }

}
