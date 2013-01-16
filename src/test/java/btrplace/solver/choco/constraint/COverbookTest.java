/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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
import btrplace.plan.Action;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Allocate;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.ShutdownVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.durationEvaluator.LinearToAResourceDuration;
import choco.kernel.solver.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link COverbook}.
 *
 * @author Fabien Hermenier
 */
public class COverbookTest extends ConstraintTestMaterial {

    /*
    @Test
    public void testInstantiation() {
        Overbook b = new Overbook(Collections.singleton(UUID.randomUUID()), "c", 1);
        COverbook c = new COverbook(b);
        Assert.assertEquals(b, c.getAssociatedConstraint());
    } */

    @Test
    public void testBasic() throws SolverException {
        UUID[] nodes = new UUID[3];
        UUID[] vms = new UUID[9];
        Mapping m = new DefaultMapping();
        ShareableResource rcCPU = new DefaultShareableResource("cpu");
        for (int i = 0; i < vms.length; i++) {
            if (i < nodes.length) {
                nodes[i] = UUID.randomUUID();
                rcCPU.set(nodes[i], 2);
                m.addOnlineNode(nodes[i]);
            }
            vms[i] = UUID.randomUUID();
            rcCPU.set(vms[i], 1);

            m.addReadyVM(vms[i]);
        }
        Model mo = new DefaultModel(m);
        mo.attach(rcCPU);
        Overbook o = new Overbook(m.getAllNodes(), "cpu", 2);
        Collection<SatConstraint> c = new HashSet<SatConstraint>();
        c.add(o);
        c.add(new Running(m.getAllVMs()));
        c.add(new Preserve(m.getAllVMs(), "cpu", 1));
        c.add(new Online(m.getAllNodes()));
        DefaultChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        cra.getSatConstraintMapper().register(new COverbook.Builder());
        cra.setTimeLimit(-1);
        ReconfigurationPlan p = cra.solve(mo, c);
        Assert.assertNotNull(p);
        Assert.assertEquals(SatConstraint.Sat.SATISFIED, o.isSatisfied(p.getResult()));
    }

    /**
     * One overbook factor per node.
     *
     * @throws SolverException should not occur
     */
    @Test
    public void testMultipleOverbook() throws SolverException {
        UUID[] nodes = new UUID[3];
        UUID[] vms = new UUID[11];
        Mapping m = new DefaultMapping();
        ShareableResource rcCPU = new DefaultShareableResource("cpu");
        for (int i = 0; i < vms.length; i++) {
            if (i < nodes.length) {
                nodes[i] = UUID.randomUUID();
                rcCPU.set(nodes[i], 2);
                m.addOnlineNode(nodes[i]);
            }
            vms[i] = UUID.randomUUID();
            rcCPU.set(vms[i], 1);

            m.addReadyVM(vms[i]);
        }
        Model mo = new DefaultModel(m);
        mo.attach(rcCPU);
        Collection<SatConstraint> c = new HashSet<SatConstraint>();
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
        //System.out.println(p);
        //System.out.println(p.getResult().getMapping());
        for (SatConstraint cstr : c) {
            Assert.assertEquals(SatConstraint.Sat.SATISFIED, cstr.isSatisfied(p.getResult()));
        }
    }

    @Test
    public void testNoSolution() throws SolverException {
        UUID[] nodes = new UUID[10];
        UUID[] vms = new UUID[31];
        Mapping m = new DefaultMapping();
        ShareableResource rcMem = new DefaultShareableResource("mem");
        for (int i = 0; i < vms.length; i++) {
            if (i < nodes.length) {
                nodes[i] = UUID.randomUUID();
                rcMem.set(nodes[i], 3);
                m.addOnlineNode(nodes[i]);
            }
            vms[i] = UUID.randomUUID();
            rcMem.set(vms[i], 1);
            m.addReadyVM(vms[i]);
        }
        Model mo = new DefaultModel(m);
        mo.attach(rcMem);
        Collection<SatConstraint> c = new HashSet<SatConstraint>();
        c.add(new Overbook(m.getAllNodes(), "mem", 1));
        c.add(new Running(m.getAllVMs()));
        c.add(new Preserve(m.getAllVMs(), "mem", 1));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getDurationEvaluators().register(BootVM.class, new LinearToAResourceDuration(rcMem, 2, 3));
        Assert.assertNull(cra.solve(mo, c));
    }

    @Test
    public void testGetMisplaced() throws SolverException {
        Mapping m = new DefaultMapping();
        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);
        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm2, n2);
        m.addRunningVM(vm3, n2);
        m.addRunningVM(vm4, n3);
        m.addRunningVM(vm5, n3);
        m.addRunningVM(vm6, n3);
        ShareableResource rcCPU = new DefaultShareableResource("cpu", 1);
        Model mo = new DefaultModel(m);
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
        Mapping m = new DefaultMapping();

        m.addOnlineNode(n1);
        m.addRunningVM(vm1, n1);
        m.addReadyVM(vm3);

        ShareableResource rcCPU = new DefaultShareableResource("cpu", 2);

        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(new Running(Collections.singleton(vm3)));
        cstrs.add(new Sleeping(Collections.singleton(vm1)));
        cstrs.add(new Online(m.getAllNodes()));
        cstrs.add(new Overbook(m.getAllNodes(), "cpu", 1));
        cstrs.add(new Preserve(m.getAllVMs(), "cpu", 2));
        Model mo = new DefaultModel(m);
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

        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);

        Model mo = new DefaultModel(map);
        ShareableResource rc = new DefaultShareableResource("foo");
        rc.set(n1, 5);
        rc.set(vm1, 3);
        rc.set(vm2, 2);
        mo.attach(rc);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
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
