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
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.Running;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.action.BootVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.durationEvaluator.LinearToAResourceDuration;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

/**
 * Unit tests for {@link COverbook}.
 *
 * @author Fabien Hermenier
 */
public class COverbookTest {

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
        DefaultChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        cra.getSatConstraintMapper().register(new COverbook.Builder());
        cra.setTimeLimit(-1);
        ReconfigurationPlan p = cra.solve(mo, c);
        Assert.assertNotNull(p);
        //System.out.println(p);
        //System.out.println(p.getResult().getMapping());
        Assert.assertTrue(o.isSatisfied(p.getResult()).equals(SatConstraint.Sat.SATISFIED));
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
        DefaultChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        cra.getSatConstraintMapper().register(new COverbook.Builder());
        cra.setTimeLimit(-1);
        ReconfigurationPlan p = cra.solve(mo, c);
        Assert.assertNotNull(p);
        //System.out.println(p);
        //System.out.println(p.getResult().getMapping());
        for (SatConstraint cstr : c) {
            Assert.assertTrue(cstr.isSatisfied(p.getResult()).equals(SatConstraint.Sat.SATISFIED));
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
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new COverbook.Builder());
        cra.getDurationEvaluators().register(BootVM.class, new LinearToAResourceDuration(rcMem, 2, 3));
        Assert.assertNull(cra.solve(mo, c));
    }

    @Test
    public void testGetMisplaced() throws SolverException {
        Mapping m = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID n3 = UUID.randomUUID();
        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);
        m.addRunningVM(UUID.randomUUID(), n1);
        m.addRunningVM(UUID.randomUUID(), n2);
        m.addRunningVM(UUID.randomUUID(), n2);
        m.addRunningVM(UUID.randomUUID(), n3);
        m.addRunningVM(UUID.randomUUID(), n3);
        m.addRunningVM(UUID.randomUUID(), n3);
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
}
