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
import btrplace.plan.event.ShutdownNode;
import btrplace.plan.event.SuspendVM;
import btrplace.safeplace.CTestCase;
import btrplace.safeplace.Constraint;
import btrplace.safeplace.spec.SpecReader;
import btrplace.safeplace.spec.term.Constant;
import btrplace.safeplace.spec.type.SetType;
import btrplace.safeplace.spec.type.VMType;
import btrplace.safeplace.verification.Verifier;
import btrplace.safeplace.verification.btrplace.ImplVerifier;
import btrplace.safeplace.verification.spec.SpecVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ElementsReducerTest {

    public Constraint makeConstraint(String id) {

        SpecReader ex = new SpecReader();
        try {
            for (Constraint x : ex.getSpecification(new File("src/main/cspec/v1.cspec")).getConstraints()) {
                if (x.id().equals(id)) {
                    return x;
                }
            }
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
        m.addOfflineNode(n1);
        m.addOnlineNode(n2);
        m.addReadyVM(vm0);
        m.addRunningVM(vm1, n2);
        m.addRunningVM(vm2, n2);

        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        p.add(new SuspendVM(vm1, n2, n2, 4, 7));
        p.add(new BootNode(n1, 5, 8));
        p.add(new SuspendVM(vm2, n2, n2, 6, 9));
        p.add(new ShutdownNode(n0, 8, 11));

        Constraint cstr = makeConstraint("lonely");

        List<Constant> in = new ArrayList<>();
        in.add(new Constant(mo.getMapping().getAllVMs(), new SetType(VMType.getInstance())));

        CTestCase tc = new CTestCase(null, "", 0, cstr, in, p, true);

        ElementsReducer er = new ElementsReducer();
        SpecVerifier v1 = new SpecVerifier();
        Verifier v2 = new ImplVerifier();
        Assert.fail();
        CTestCase r = er.reduce(tc, v1, v2, null);
        System.out.println(tc);
        System.out.println(r);
        Assert.assertEquals(r.getPlan().getOrigin().getMapping().getNbNodes(), 1);
    }
}
