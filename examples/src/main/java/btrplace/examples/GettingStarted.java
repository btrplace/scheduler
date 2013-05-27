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

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.*;
import btrplace.model.view.ShareableResource;
import btrplace.plan.DependencyBasedPlanApplier;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.TimeBasedPlanApplier;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple tutorial about the usage of Btrplace.
 * The document associated to the tutorial is available
 * on <a href="https://github.com/fhermeni/btrplace-solver/wiki/GettingStarted">btrplace website</a>.
 *
 * @author Fabien Hermenier
 */
public class GettingStarted implements Example {

    private static int vm1 = new int
    (0,1);
    private static int vm2 = new int
    (0,2);
    private static int vm3 = new int
    (0,3);
    private static int vm4 = new int
    (0,4);
    private static int vm5 = new int
    (0,5);
    private static int vm6 = new int
    (0,6);

    private static int n1 = new int
    (1,1);
    private static int n2 = new int
    (1,2);
    private static int n3 = new int
    (1,3);
    private static int n4 = new int
    (1,4);

    /**
     * Make the element mapping that depicts
     * the element state and the VM positions.
     */
    public static Mapping makeMapping() {
        Mapping map = new DefaultMapping();

        //4 online nodes
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOnlineNode(n4);

        //5 VMs are currently running on the nodes
        map.addRunningVM(vm3, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm1, n3);
        map.addRunningVM(vm4, n3);
        map.addRunningVM(vm6, n4);

        //VM5 is ready to be running on a node.
        map.addReadyVM(vm5);


        return map;
    }

    /**
     * Declare the physical number of CPUs available on the nodes
     * and the number of virtual CPUs that are currently used by the VMs.
     */
    private static ShareableResource makeCPUResourceView() {
        ShareableResource rc = new ShareableResource("cpu");
        rc.set(n1, 8);
        rc.set(n2, 8);
        rc.set(n3, 8);
        rc.set(n4, 8);

        rc.set(vm1, 2);
        rc.set(vm2, 3);
        rc.set(vm3, 4);
        rc.set(vm4, 3);
        rc.set(vm6, 5);

        return rc;
    }

    /**
     * Declare the physical number of CPUs available on the nodes
     * and the number of virtual CPUs that are currently used by the VMs.
     */
    private static ShareableResource makeMemResourceView() {
        ShareableResource rc = new ShareableResource("mem");
        rc.set(n1, 7);
        rc.set(n2, 7);
        rc.set(n3, 7);
        rc.set(n4, 7);

        rc.set(vm1, 2);
        rc.set(vm2, 2);
        rc.set(vm3, 4);
        rc.set(vm4, 3);
        rc.set(vm6, 4);

        return rc;
    }

    private static Set<SatConstraint> makeConstraints() {
        Set<SatConstraint> cstrs = new HashSet<>();

        //VMs VM2 and VM3 must be running on distinct nodes
        cstrs.add(new Spread(new HashSet<>(Arrays.asList(vm2, vm3))));

        //VM VM1 must have at least 3 virtual CPU dedicated to it
        cstrs.add(new Preserve(Collections.singleton(vm1), "cpu", 3));

        //node N4 must be set offline
        cstrs.add(new Offline(Collections.singleton(n4)));

        //VM5 must be running, It asks for 3 cpu and 2 mem resources
        cstrs.add(new Running(Collections.singleton(vm5)));
        cstrs.add(new Preserve(Collections.singleton(vm5), "cpu", 3));
        cstrs.add(new Preserve(Collections.singleton(vm5), "mem", 2));

        //VM4 must be turned off, i.e. set back to the ready state
        cstrs.add(new Ready(Collections.singleton(vm4)));
        return cstrs;
    }

    @Override
    public boolean run() {
        Mapping map = makeMapping();

        //Now, we declare views related to
        //the memory and the cpu resources
        ShareableResource rcCPU = makeCPUResourceView();
        ShareableResource rcMem = makeMemResourceView();

        //We create a model that aggregates the mapping and the views
        Model origin = new DefaultModel(map);
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
