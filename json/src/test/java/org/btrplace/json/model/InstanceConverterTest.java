/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model;

import org.btrplace.json.JSONConverterException;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Online;
import org.btrplace.model.constraint.Running;
import org.btrplace.model.constraint.SatConstraint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link InstanceConverter}.
 *
 * @author Fabien Hermenier
 */
public class InstanceConverterTest {

    @Test
    public void testConversion() throws JSONConverterException {
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();
        Node n1 = mo.newNode();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        ma.addOnlineNode(n1);
        ma.addOfflineNode(n1);
        ma.addReadyVM(vm1);
        ma.addReadyVM(vm2);

        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Online.newOnline(ma.getAllNodes()));
        cstrs.add(new Running(vm2));
        Instance i = new Instance(mo, cstrs, new MinMTTR());

        InstanceConverter conv = new InstanceConverter();
        String o = conv.toJSONString(i);
        System.out.println(o);
        Instance res = conv.fromJSON(o);
        Assert.assertEquals(i, res);
    }

}
