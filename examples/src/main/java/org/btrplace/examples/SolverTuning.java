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

package org.btrplace.examples;

import org.btrplace.model.*;
import org.btrplace.model.constraint.Overbook;
import org.btrplace.model.constraint.Preserve;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.duration.LinearToAResourceActionDuration;

import java.util.*;

/**
 * Tutorial about the basic tuning of a {@link org.btrplace.scheduler.choco.ChocoScheduler}.
 *
 * @author Fabien Hermenier
 * @see <a href="https://github.com/btrplace/scheduler/wiki/Tuning-the-Reconfiguration-Algorithm">btrplace website</a>
 */
public class SolverTuning implements Example {

    private List<Node> nodes;

    @Override
    public String toString() {
        return "Solver Tuning";
    }

    @Override
    public boolean run() {

        //Make a default model with 500 nodes hosting 3,000 VMs
        Model model = makeModel();

        Set<SatConstraint> constraints = new HashSet<>();
        //We allow memory over-commitment with a overbooking ratio of 50%
        //i.e. 1MB physical RAM for 1.5MB virtual RAM
        constraints.addAll(Overbook.newOverbooks(model.getMapping().getAllNodes(), "mem", 1.5));

        /**
         * On 10 nodes, 4 of the 6 hosted VMs ask now for a 4GB bandwidth
         */
        for (int i = 0; i < 5; i++) {
            Node n = nodes.get(i);
            Set<VM> vmsOnN = model.getMapping().getRunningVMs(n);
            Iterator<VM> ite = vmsOnN.iterator();
            for (int j = 0; ite.hasNext() && j < 4; j++) {
                VM v = ite.next();
                constraints.add(new Preserve(v, "bandwidth", 4));
            }
        }

        ChocoScheduler cra = new DefaultChocoScheduler();

        //Customize the estimated duration of actions
        cra.getDurationEvaluators().register(MigrateVM.class, new LinearToAResourceActionDuration<VM>("mem", 1, 3));

        //We want the best possible solution, computed in up to 5 sec.
        cra.doOptimize(true);
        cra.setTimeLimit(5);
        //We solve without the repair mode
        cra.doRepair(false);
        solve(cra, model, constraints);

        //Re-solve using the repair mode to check for the improvement
        cra.doRepair(true);
        solve(cra, model, constraints);
        return true;
    }

    private void solve(ChocoScheduler cra, Model model, Set<SatConstraint> constraints) {
        try {
            ReconfigurationPlan p = cra.solve(model, constraints);
            if (p != null) {
                System.out.println("--- Solving using repair : " + cra.doRepair());
                System.out.println(cra.getStatistics());
            }
        } catch (SchedulerException e) {
            System.err.println("--- Solving using repair : " + cra.doRepair() + "; Error: " + e.getMessage());
            System.err.flush();
        }
        System.out.flush();
    }

    /**
     * A default model with 100 nodes hosting 600 VMs.
     * 6 VMs per node
     * Each node has a 10GB network interface and 32 GB RAM
     * Each VM consumes 1GB Bandwidth and between 1 to 5 GB RAM
     */
    private Model makeModel() {
        Model mo = new DefaultModel();
        Mapping mapping = mo.getMapping();

        int nbNodes = 1000;
        int nbVMs = 6 * nbNodes;

        //Memory usage/consumption in GB
        ShareableResource rcMem = new ShareableResource("mem");

        //A resource representing the bandwidth usage/consumption of the elements in GB
        ShareableResource rcBW = new ShareableResource("bandwidth");

        nodes = new ArrayList<>(nbNodes);

        for (int i = 0; i < nbNodes; i++) {
            Node n = mo.newNode();
            nodes.add(n);
            mapping.addOnlineNode(n);

            //Each node provides a 10GB bandwidth and 32 GB RAM to its VMs
            rcBW.setCapacity(n, 10);
            rcMem.setCapacity(n, 32);
        }

        for (int i = 0; i < nbVMs; i++) {
            VM vm = mo.newVM();
            //Basic balancing through a round-robin: 6 VMs per node
            mapping.addRunningVM(vm, nodes.get(i % nodes.size()));

            //Each VM uses currently a 1GB bandwidth and 1,2 or 3 GB RAM
            rcBW.setConsumption(vm, 1);
            rcMem.setConsumption(vm, i % 5 + 1);
        }

        mo.attach(rcBW);
        mo.attach(rcMem);
        return mo;
    }
}
