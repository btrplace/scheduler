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

package btrplace.solver.api.cstrSpec.reducer;

import btrplace.model.*;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.type.NodeType;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.verification.Verifier;
import btrplace.solver.api.cstrSpec.verification.btrplace.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.spec.SpecVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class SignatureReducerTest {

    Node n0;
    Node n1;
    Node n2;
    Node n3;
    Node n4;

    VM vm0;
    VM vm1;
    VM vm2;
    VM vm3;
    VM vm4;

    private ReconfigurationPlan makePlan() {

        Model mo = new DefaultModel();
        n0 = mo.newNode();
        n1 = mo.newNode();
        n2 = mo.newNode();
        n3 = mo.newNode();
        n4 = mo.newNode();

        vm0 = mo.newVM();
        vm1 = mo.newVM();
        vm2 = mo.newVM();
        vm3 = mo.newVM();
        vm4 = mo.newVM();

        Mapping m = mo.getMapping();
        m.addOnlineNode(n0);
        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);
        m.addOnlineNode(n4);

        m.addRunningVM(vm0, n1);
        m.addRunningVM(vm1, n2);
        m.addRunningVM(vm2, n2);
        m.addSleepingVM(vm3, n2);
        m.addReadyVM(vm4);

        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        p.add(new ShutdownVM(vm0, n1, 0, 3));
        p.add(new SuspendVM(vm1, n2, n2, 0, 3));
        return p;
    }

    public Constraint makeConstraint(String id) {

        SpecReader ex = new SpecReader();
        try {
            for (btrplace.solver.api.cstrSpec.Constraint x : ex.getSpecification(new File("src/main/cspec/v1.cspec")).getConstraints()) {
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
        ReconfigurationPlan p = makePlan();
        Constraint c = makeConstraint("offline");
        System.out.println(p.getOrigin().getMapping() + "\n" + p);
        System.out.println(c.pretty());
        List<Constant> args = new ArrayList<>();

        args.add(new Constant(p.getOrigin().getMapping().getAllNodes(), new SetType(NodeType.getInstance())));

        /*Set<Set<Node>> ps = new HashSet<>();
        ps.add(new HashSet<>(Arrays.asList(n0, n1)));
        ps.add(new HashSet<>(Arrays.asList(n2, n3, n4)));
        args.add(new Constant(ps, new SetType(new SetType(NodeType.getInstance()))));
        args.add(BoolType.getInstance().newValue(true));                             */

        //CTestCase tc = new CTestCase("foo", c, args, p, false);
        SignatureReducer er = new SignatureReducer();
        Verifier v1 = new SpecVerifier();
        //v1.continuous(false);
        Verifier v2 = new ImplVerifier();
        //v2.continuous(false);
        //CTestCase r = er.reduce(tc, v1, v2);
        Assert.fail();
    }

    @Test
    public void test2() throws Exception {
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

        Constraint cstr = makeConstraint("offline");

        List<Constant> in = new ArrayList<>();
        in.add(new Constant(Collections.singletonList(n1), new SetType(NodeType.getInstance())));

        //CTestCase tc = new CTestCase("foo", cstr, in, p, false);
        SignatureReducer er = new SignatureReducer();
        Verifier v1 = new SpecVerifier();
        //v1.continuous(false);
        Verifier v2 = new ImplVerifier();
        //v2.continuous(false);
        //CTestCase r = er.reduce(tc, v1, v2);

        //System.out.println(tc);
        //System.out.println(r);
        Assert.fail();

    }
}
