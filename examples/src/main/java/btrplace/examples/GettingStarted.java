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

package btrplace.examples;

import btrplace.model.*;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.DependencyBasedPlanApplier;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.TimeBasedPlanApplier;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;

import java.util.*;

/**
 * Simple tutorial about the usage of Btrplace.
 * The document associated to the tutorial is available
 * on <a href="https://github.com/fhermeni/btrplace-solver/wiki/GettingStarted">btrplace website</a>.
 *
 * @author Fabien Hermenier
 */
public class GettingStarted implements Example {

    private List<VM> vms;
    private List<Node> nodes;
    /*private static int vm1 = 1;
    private static int vm2 = 2;
    private static int vm3 = 3;
    private static int vm4 = 4;
    private static int vm5 = 5;
    private static int vm6 = 6;

    private static int n1 = -1;
    private static int n2 = -2;
    private static int n3 = -3;
    private static int n4 = -4;       */

    public GettingStarted() {
        vms = new ArrayList<>();
        nodes = new ArrayList<>();
    }

    /**
     * Make the element mapping that depicts
     * the element state and the VM positions.
     */
    public Mapping makeMapping(Model o) {
        Mapping map = o.getMapping();

        //4 online nodes
        for (int i = 0; i < 4; i++) {
            nodes.add(o.newNode());
        }
        map.addOnlineNode(nodes.get(0));
        map.addOnlineNode(nodes.get(1));
        map.addOnlineNode(nodes.get(2));
        map.addOnlineNode(nodes.get(3));

        //5 VMs are currently running on the nodes
        for (int i = 0; i < 6; i++) {
            vms.add(o.newVM());
        }

        map.addRunningVM(vms.get(2), nodes.get(0));
        map.addRunningVM(vms.get(1), nodes.get(1));
        map.addRunningVM(vms.get(0), nodes.get(2));
        map.addRunningVM(vms.get(3), nodes.get(2));
        map.addRunningVM(vms.get(5), nodes.get(3));

        //VM5 is ready to be running on a node.
        map.addReadyVM(vms.get(4));


        return map;
    }

    /**
     * Declare the physical number of CPUs available on the nodes
     * and the number of virtual CPUs that are currently used by the VMs.
     */
    private ShareableResource makeCPUResourceView() {
        ShareableResource rc = new ShareableResource("cpu");
        rc.setCapacity(nodes.get(0), 8);
        rc.setCapacity(nodes.get(1), 8);
        rc.setCapacity(nodes.get(2), 8);
        rc.setCapacity(nodes.get(3), 8);

        rc.setConsumption(vms.get(0), 2);
        rc.setConsumption(vms.get(1), 3);
        rc.setConsumption(vms.get(2), 4);
        rc.setConsumption(vms.get(3), 3);
        rc.setConsumption(vms.get(5), 5);

        return rc;
    }

    /**
     * Declare the physical number of CPUs available on the nodes
     * and the number of virtual CPUs that are currently used by the VMs.
     */
    private ShareableResource makeMemResourceView() {
        ShareableResource rc = new ShareableResource("mem");
        rc.setCapacity(nodes.get(0), 7);
        rc.setCapacity(nodes.get(1), 7);
        rc.setCapacity(nodes.get(2), 7);
        rc.setCapacity(nodes.get(3), 7);

        rc.setConsumption(vms.get(0), 2);
        rc.setConsumption(vms.get(1), 2);
        rc.setConsumption(vms.get(2), 4);
        rc.setConsumption(vms.get(3), 3);
        rc.setConsumption(vms.get(5), 4);

        return rc;
    }

    private Set<SatConstraint> makeConstraints() {
        Set<SatConstraint> cstrs = new HashSet<>();

        //VMs VM2 and VM3 must be running on distinct nodes
        cstrs.add(new Spread(new HashSet<>(Arrays.asList(vms.get(1), vms.get(2)))));

        //VM VM1 must have at least 3 virtual CPU dedicated to it
        cstrs.add(new Preserve(Collections.singleton(vms.get(0)), "cpu", 3));

        //node N4 must be set offline
        cstrs.add(new Offline(Collections.singleton(nodes.get(3))));

        //VM5 must be running, It asks for 3 cpu and 2 mem resources
        cstrs.add(new Running(Collections.singleton(vms.get(4))));
        cstrs.add(new Preserve(Collections.singleton(vms.get(4)), "cpu", 3));
        cstrs.add(new Preserve(Collections.singleton(vms.get(4)), "mem", 2));

        //VM4 must be turned off, i.e. set back to the ready state
        cstrs.add(new Ready(Collections.singleton(vms.get(3))));
        return cstrs;
    }

    @Override
    public boolean run() {
        Model origin = new DefaultModel();
        Mapping map = makeMapping(origin);

        //Now, we declare views related to
        //the memory and the cpu resources
        ShareableResource rcCPU = makeCPUResourceView();
        ShareableResource rcMem = makeMemResourceView();

        //We create a model that aggregates the mapping and the views
        origin.attach(rcCPU);
        origin.attach(rcMem);

        Set<SatConstraint> cstrs = makeConstraints();

        ChocoReconfigurationAlgorithm ra = new DefaultChocoReconfigurationAlgorithm();
        try {
            ReconfigurationPlan plan = ra.solve(origin, cstrs);
            System.out.println("Time-based plan:");
            System.out.println(new TimeBasedPlanApplier().toString(plan));
            System.out.println("\nDependency based plan:");
            System.out.println(new DependencyBasedPlanApplier().toString(plan));
            return (plan != null);
        } catch (SolverException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "GettingStarted";
    }
}
