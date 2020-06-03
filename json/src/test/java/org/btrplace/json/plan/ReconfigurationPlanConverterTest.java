/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.plan;

import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.ModelConverter;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.BootNode;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.MigrateVM;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ReconfigurationPlanConverter}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanConverterTest {

    @Test
    public void testAccessors() {
        ModelConverter mc = new ModelConverter();
        ReconfigurationPlanConverter rcp = new ReconfigurationPlanConverter(mc);
        Assert.assertEquals(rcp.getModelConverter(), mc);
    }

    @Test
    public void testConversion() throws JSONConverterException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOfflineNode(n2);
        map.addOnlineNode(n3);
        map.addReadyVM(vm1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n1);
        map.addSleepingVM(vm4, n3);
        map.addRunningVM(vm5, n3);


        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        plan.add(new MigrateVM(vm2, n1, n3, 0, 1));
        plan.add(new BootVM(vm1, n3, 1, 2));
        plan.add(new BootNode(n2, 2, 5));
        plan.add(new Allocate(vm1, n3, "foo", 5, 3, 5));

        ReconfigurationPlanConverter rcp = new ReconfigurationPlanConverter();
        String j = rcp.toJSONString(plan);
        ReconfigurationPlan p2 = rcp.fromJSON(j);
        Assert.assertEquals(p2, plan);
    }
}
