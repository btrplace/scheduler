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
import org.btrplace.model.constraint.*;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.DependencyBasedPlanApplier;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.TimeBasedPlanApplier;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Simple tutorial about the usage of Btrplace.
 *
 * @author Fabien Hermenier
 * @see <a href="https://github.com/btrplace/scheduler/wiki/GettingStarted">btrplace website</a>
 */
public class GettingStarted implements Example {

    private List<VM> vms = new ArrayList<>();
    private List<Node> nodes = new ArrayList<>();

    /**
     * Make a model with 4 online nodes, 6 VMs (5 running, 1 ready).
     * Declare 2 resources.
     */
    private Model makeModel() {
        Model model = new DefaultModel();
        Mapping map = model.getMapping();

        //Create 4 online nodes
        for (int i = 0; i < 4; i++) {
            Node n = model.newNode();
            nodes.add(n);
            map.addOnlineNode(n);
        }

        //Create 6 VMs: vm0..vm5
        for (int i = 0; i < 6; i++) {
            VM v = model.newVM();
            vms.add(v);
        }

        //vm2,vm1,vm0,vm3,vm5 are running on the nodes
        map.addRunningVM(vms.get(2), nodes.get(0));
        map.addRunningVM(vms.get(1), nodes.get(1));
        map.addRunningVM(vms.get(0), nodes.get(2));
        map.addRunningVM(vms.get(3), nodes.get(2));
        map.addRunningVM(vms.get(5), nodes.get(3));

        //vm4 is ready to be running on a node.
        map.addReadyVM(vms.get(4));

        //Declare a view to specify the "cpu" physical capacity of the nodes
        // and the virtual consumption of the VMs.
        //By default, nodes have 8 "cpu" resources
        ShareableResource rcCPU = new ShareableResource("cpu", 8, 0);
        rcCPU.setConsumption(vms.get(0), 2);
        rcCPU.setConsumption(vms.get(1), 3);
        rcCPU.setConsumption(vms.get(2), 4);
        rcCPU.setConsumption(vms.get(3), 3);
        rcCPU.setConsumption(vms.get(5), 5);

        //By default, nodes have 7 "mem" resources
        ShareableResource rcMem = new ShareableResource("mem", 7, 0);
        rcMem.setConsumption(vms.get(0), 2);
        rcMem.setConsumption(vms.get(1), 2);
        rcMem.setConsumption(vms.get(2), 4);
        rcMem.setConsumption(vms.get(3), 3);
        rcMem.setConsumption(vms.get(5), 4);

        //Attach the resources
        model.attach(rcCPU);
        model.attach(rcMem);
        return model;
    }

    /**
     * Declare some constraints.
     */
    public List<SatConstraint> makeConstraints() {
        List<SatConstraint> cstrs = new ArrayList<>();
        //VM1 and VM2 must be running on distinct nodes
        cstrs.add(new Spread(new HashSet<>(Arrays.asList(vms.get(1), vms.get(2)))));

        //VM0 must have at least 3 virtual CPU dedicated to it
        cstrs.add(new Preserve(vms.get(0), "cpu", 3));

        //N3 must be set offline
        cstrs.add(new Offline(nodes.get(3)));

        //VM4 must be running, It asks for 3 cpu and 2 mem resources
        cstrs.add(new Running(vms.get(4)));
        cstrs.add(new Preserve(vms.get(4), "cpu", 3));
        cstrs.add(new Preserve(vms.get(4), "mem", 2));

        //VM3 must be turned off, i.e. set back to the ready state
        cstrs.add(new Ready(vms.get(3)));
        return cstrs;
    }

    @Override
    public boolean run() {

        Model model = makeModel();
        List<SatConstraint> cstrs = makeConstraints();

        ChocoScheduler ra = new DefaultChocoScheduler();
        try {
            ReconfigurationPlan plan = ra.solve(model, cstrs);
            System.out.println("Time-based plan:");
            System.out.println(new TimeBasedPlanApplier().toString(plan));
            System.out.println("\nDependency based plan:");
            System.out.println(new DependencyBasedPlanApplier().toString(plan));
            return (plan != null);
        } catch (SchedulerException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "GettingStarted";
    }
}
