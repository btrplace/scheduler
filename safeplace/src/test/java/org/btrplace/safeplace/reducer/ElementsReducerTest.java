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

package org.btrplace.safeplace.reducer;

import org.btrplace.model.*;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.BootNode;
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.plan.event.SuspendVM;
import org.btrplace.safeplace.CTestCase;
import org.btrplace.safeplace.Constraint;
import org.btrplace.safeplace.Specification;
import org.btrplace.safeplace.spec.SpecExtractor;
import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.spec.type.SetType;
import org.btrplace.safeplace.spec.type.VMType;
import org.btrplace.safeplace.verification.Verifier;
import org.btrplace.safeplace.verification.btrplace.ImplVerifier;
import org.btrplace.safeplace.verification.spec.SpecVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ElementsReducerTest {

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
