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
import btrplace.model.constraint.Oversubscription;
import btrplace.model.constraint.Running;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.SolverException;
import btrplace.plan.action.BootVM;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.durationEvaluator.LinearToAResourceDuration;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.Random;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: fhermeni
 * Date: 23/11/12
 * Time: 22:56
 * To change this template use File | Settings | File Templates.
 */
public class COversubscriptionTest {

    @Test
    public void testBasic() throws SolverException {
        Random rnd = new Random();
        UUID[] nodes = new UUID[10];
        UUID[] vms = new UUID[30];
        Mapping m = new DefaultMapping();
        IntResource rcMem = new DefaultIntResource("mem");
        IntResource rcCPU = new DefaultIntResource("cpu");
        for (int i = 0; i < vms.length; i++) {
            if (i < nodes.length) {
                nodes[i] = UUID.randomUUID();
                rcMem.set(nodes[i], 10);
                rcCPU.set(nodes[i], 4);
                m.addOnlineNode(nodes[i]);
            }
            vms[i] = UUID.randomUUID();
            rcMem.set(vms[i], rnd.nextInt(3));
            rcCPU.set(vms[i], rnd.nextInt(2));

            m.addWaitingVM(vms[i]);
        }
        Model mo = new DefaultModel(m);
        mo.attach(rcCPU);
        mo.attach(rcMem);
        mo.attach(new Oversubscription(m.getAllNodes(), "cpu", 2));
        mo.attach(new Oversubscription(m.getAllNodes(), "mem", 1));
        mo.attach(new Running(m.getAllVMs()));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new COversubscription.Builder());
        cra.getDurationEvaluators().register(BootVM.class, new LinearToAResourceDuration(rcMem, 2, 3));
        ReconfigurationPlan p = cra.solve(mo);
    }

    @Test
    public void testNoSolution() throws SolverException {
        UUID[] nodes = new UUID[10];
        UUID[] vms = new UUID[31];
        Mapping m = new DefaultMapping();
        IntResource rcMem = new DefaultIntResource("mem");
        for (int i = 0; i < vms.length; i++) {
            if (i < nodes.length) {
                nodes[i] = UUID.randomUUID();
                rcMem.set(nodes[i], 3);
                m.addOnlineNode(nodes[i]);
            }
            vms[i] = UUID.randomUUID();
            rcMem.set(vms[i], 1);
            m.addWaitingVM(vms[i]);
        }
        Model mo = new DefaultModel(m);
        mo.attach(rcMem);
        mo.attach(new Oversubscription(m.getAllNodes(), "mem", 1));
        mo.attach(new Running(m.getAllVMs()));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.getSatConstraintMapper().register(new COversubscription.Builder());
        cra.getDurationEvaluators().register(BootVM.class, new LinearToAResourceDuration(rcMem, 2, 3));
        Assert.assertNull(cra.solve(mo));
    }
}
