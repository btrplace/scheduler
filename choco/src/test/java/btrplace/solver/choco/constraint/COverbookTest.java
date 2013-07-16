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

package btrplace.solver.choco.constraint;

import btrplace.model.*;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.plan.event.Allocate;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.ShutdownVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingFiller;
import btrplace.solver.choco.durationEvaluator.LinearToAResourceActionDuration;
import choco.kernel.solver.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link COverbook}.
 *
 * @author Fabien Hermenier
 */
public class COverbookTest {

    @Test
    public void testBasic() throws SolverException {
        Node[] nodes = new Node[3];
        VM[] vms = new VM[9];
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        btrplace.model.view.ShareableResource rcCPU = new ShareableResource("cpu");
        for (int i = 0; i < vms.length; i++) {
            if (i < nodes.length) {
                nodes[i] = mo.newNode();
                rcCPU.setCapacity(nodes[i], 2);
                m.addOnlineNode(nodes[i]);
            }
            vms[i] = mo.newVM();
            rcCPU.setConsumption(vms[i], 1);

            m.addReadyVM(vms[i]);
        }
        mo.attach(rcCPU);
        Overbook o = new Overbook(m.getAllNodes(), "cpu", 2);
        Collection<SatConstraint> c = new HashSet<>();
        c.add(o);
        c.add(new Running(m.getAllVMs()));
        c.add(new Preserve(m.getAllVMs(), "cpu", 1));
        c.add(new Online(m.getAllNodes()));
        DefaultChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        //cra.setVerbosity(1);
        cra.getConstraintMapper().register(new COverbook.Builder());
        ReconfigurationPlan p = cra.solve(mo, c);
        Assert.assertNotNull(p);
    }

    /**
     * One overbook factor per node.
     *
     * @throws SolverException should not occur
     */
    @Test
    public void testMultipleOverbook() throws SolverException {
        Node[] nodes = new Node[3];
        VM[] vms = new VM[11];
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        ShareableResource rcCPU = new ShareableResource("cpu");
        for (int i = 0; i < vms.length; i++) {
            if (i < nodes.length) {
                nodes[i] = mo.newNode();
                rcCPU.setCapacity(nodes[i], 2);
                m.addOnlineNode(nodes[i]);
            }
            vms[i] = mo.newVM();
            rcCPU.setConsumption(vms[i], 1);

            m.addReadyVM(vms[i]);
        }
        mo.attach(rcCPU);
        Collection<SatConstraint> c = new HashSet<>();
        c.add(new Overbook(Collections.singleton(nodes[0]), "cpu", 1));
        c.add(new Overbook(Collections.singleton(nodes[1]), "cpu", 2));
        c.add(new Overbook(Collections.singleton(nodes[2]), "cpu", 3));
        c.add(new Running(m.getAllVMs()));
        c.add(new Preserve(m.getAllVMs(), "cpu", 1));
        DefaultChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        cra.setTimeLimit(-1);
        ReconfigurationPlan p = cra.solve(mo, c);
        Assert.assertNotNull(p);
        /*for (SatConstraint cstr : c) {
            Assert.assertEquals(SatConstraint.Sat.SATISFIED, cstr.isSatisfied(p.getResult()));
        } */
    }

    @Test
    public void testNoSolution() throws SolverException {
        Node[] nodes = new Node[10];
        VM[] vms = new VM[31];
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        ShareableResource rcMem = new ShareableResource("mem");
        for (int i = 0; i < vms.length; i++) {
            if (i < nodes.length) {
                nodes[i] = mo.newNode();
                rcMem.setCapacity(nodes[i], 3);
                m.addOnlineNode(nodes[i]);
            }
            vms[i] = mo.newVM();
            rcMem.setConsumption(vms[i], 1);
            m.addReadyVM(vms[i]);
        }
        mo.attach(rcMem);
        Collection<SatConstraint> c = new HashSet<>();
        c.add(new Overbook(m.getAllNodes(), "mem", 1));
        c.add(new Running(m.getAllVMs()));
        c.add(new Preserve(m.getAllVMs(), "mem", 1));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getDurationEvaluators().register(BootVM.class, new LinearToAResourceActionDuration<VM>("mem", 2, 3));
        Assert.assertNull(cra.solve(mo, c));
    }

    @Test
    public void testGetMisplaced() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        VM vm6 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Mapping m = new MappingFiller(mo.getMapping()).on(n1, n2, n3)
                .run(n1, vm1)
                .run(n2, vm2, vm3)
                .run(n3, vm4, vm5, vm6).get();
        ShareableResource rcCPU = new ShareableResource("cpu", 1, 1);
        mo.attach(rcCPU);
        Overbook o1 = new Overbook(Collections.singleton(n1), "cpu", 1);
        Overbook o2 = new Overbook(Collections.singleton(n2), "cpu", 2);
        Overbook o3 = new Overbook(Collections.singleton(n3), "cpu", 3);
        COverbook co1 = new COverbook(o1);
        COverbook co2 = new COverbook(o2);
        COverbook co3 = new COverbook(o3);
        Assert.assertTrue(co1.getMisPlacedVMs(mo).isEmpty());
        Assert.assertTrue(co2.getMisPlacedVMs(mo).isEmpty());
        Assert.assertEquals(o3.getInvolvedVMs(), co3.getMisPlacedVMs(mo));
    }


    @Test
    public void testWithScheduling1() throws SolverException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Mapping m = new MappingFiller(mo.getMapping()).on(n1).run(n1, vm1).ready(vm3).get();

        ShareableResource rcCPU = new ShareableResource("cpu", 2, 2);

        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Running(Collections.singleton(vm3)));
        cstrs.add(new Sleeping(Collections.singleton(vm1)));
        cstrs.add(new Online(m.getAllNodes()));
        cstrs.add(new Overbook(m.getAllNodes(), "cpu", 1));
        cstrs.add(new Preserve(m.getAllVMs(), "cpu", 2));
        mo.attach(rcCPU);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        ReconfigurationPlan p = cra.solve(mo, cstrs);

        Assert.assertNotNull(p);
    }

    /**
     * Test with a root VM that has increasing need and another one that prevent it
     * to get the resources immediately
     */
    @Test
    public void testWithIncrease() throws SolverException, ContradictionException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        Node n1 = mo.newNode();
        Mapping map = new MappingFiller(mo.getMapping()).on(n1).run(n1, vm1, vm2).get();

        btrplace.model.view.ShareableResource rc = new ShareableResource("foo");
        rc.setCapacity(n1, 5);
        rc.setConsumption(vm1, 3);
        rc.setConsumption(vm2, 2);
        mo.attach(rc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Online(map.getAllNodes()));
        Overbook o = new Overbook(map.getAllNodes(), "foo", 1);
        o.setContinuous(true);
        cstrs.add(o);
        cstrs.add(new Ready(Collections.singleton(vm2)));
        cstrs.add(new Preserve(Collections.singleton(vm1), "foo", 5));
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 2);
        //An allocate action at the moment the vm2 leaved.
        Action al = null;
        Action sh = null;
        for (Action a : p) {
            if (a instanceof Allocate) {
                al = a;
            } else if (a instanceof ShutdownVM) {
                sh = a;
            } else {
                Assert.fail();
            }
        }
        Assert.assertTrue(sh.getEnd() <= al.getStart());
    }
}
