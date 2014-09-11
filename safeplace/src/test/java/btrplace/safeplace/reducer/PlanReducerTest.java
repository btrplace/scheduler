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

package btrplace.safeplace.reducer;

import btrplace.model.*;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownNode;
import btrplace.safeplace.Constraint;
import btrplace.safeplace.Specification;
import btrplace.safeplace.spec.SpecExtractor;
import btrplace.safeplace.spec.term.Constant;
import btrplace.safeplace.verification.Verifier;
import btrplace.safeplace.verification.btrplace.ImplVerifier;
import btrplace.safeplace.verification.spec.SpecVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class PlanReducerTest {

    public Constraint makeConstraint(String id) {
        SpecExtractor ex = new SpecExtractor();
        try {
            Specification spec = ex.extract();
            return spec.get(id);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        return null;
    }

    @Test
    public void test() throws Exception {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        VM vm0 = mo.newVM();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();

        Mapping m = mo.getMapping();
        m.addOnlineNode(n0);
        m.addOnlineNode(n1);
        m.addOfflineNode(n2);
        m.addRunningVM(vm0, n0);
        m.addReadyVM(vm1);
        m.addSleepingVM(vm2, n1);

        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        p.add(new ShutdownNode(n1, 0, 3));
        p.add(new BootVM(vm1, n0, 2, 7));
        p.add(new BootNode(n2, 0, 3));
        p.add(new MigrateVM(vm0, n0, n2, 4, 10));

        Constraint c = makeConstraint("noVMsOnOfflineNodes");
        List<Constant> in = new ArrayList<>();

        PlanReducer pr = new PlanReducer();

        //CTestCase tc = new CTestCase("foo", c, in, p);
        PlanReducer er = new PlanReducer();
        Verifier v1 = new SpecVerifier();
//        v1.continuous(true);
//        v1.continuous(false);
        Verifier v2 = new ImplVerifier();
//        v2.continuous(true);
        //CTestCase r = er.reduce(tc, v1, v2);
        Assert.fail();
        /*System.out.println(tc);
        System.out.println(r);
        Assert.assertEquals(r.getPlan().getSize(), 1);*/
    }
}
