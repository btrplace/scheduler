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
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.MigrateVM;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.durationEvaluator.DurationEvaluator;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import btrplace.solver.choco.durationEvaluator.LinearToAResourceDuration;

import java.util.*;

/**
 * Tutorial about the basic tuning of a model.
 * The document associated to the tutorial is available
 * on <a href="https://github.com/fhermeni/btrplace-solver/wiki/Customizing-a-model">btrplace website</a>.
 *
 * @author Fabien Hermenier
 */
public class ModelCustomization implements Example {

    private int n1 = new int
    (1,1);
    private int n2 = new int
    (1,2);
    private int vm1 = new int
    (2,1);
    private int vm2 = new int
    (2,2);
    private int vm3 = new int
    (2,3);
    private int vm4 = new int
    (2,4);
    private int vm5 = new int
    (2,5);
    private int vm6 = new int
    (2,6);
    private int vm7 = new int
    (2,7);
    private int vm8 = new int
    (2,8);
    private int vm9 = new int
    (2,9);
    private int vm10 = new int
    (2,10);

    private Set<Integer> g1 = new HashSet<>(Arrays.asList(vm1, vm2, vm4));
    private Set<Integer> g2 = new HashSet<>(Arrays.asList(vm5, vm6, vm8));

    /**
     * We customize the estimate duration of the VM migration action
     * to be equals to 2 second per GB of memory plus 3 seconds
     */
    class MyMigrationEvaluator implements DurationEvaluator {

        ShareableResource rc;

        MyMigrationEvaluator(ShareableResource rcMem) {
            rc = rcMem;
        }

        @Override
        public int evaluate(Model mo, int e) {
            return rc.get(e) * 2 + 3;
        }

    }

    private Model makeModel() {

        Mapping map = new DefaultMapping();

        map.addOnlineNode(n1);
        map.addOnlineNode(n2);

        ShareableResource rcMem = new ShareableResource("mem", 32); //32GB by default

        rcMem.set(vm1, 1).set(vm2, 2).set(vm3, 3)
                .set(vm4, 1).set(vm5, 2).set(vm6, 3)
                .set(vm7, 1).set(vm8, 2).set(vm9, 3).set(vm10, 1);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n1);
        map.addRunningVM(vm4, n1);
        map.addRunningVM(vm5, n1);
        map.addRunningVM(vm6, n1);
        map.addRunningVM(vm7, n2);
        map.addRunningVM(vm8, n2);
        map.addRunningVM(vm9, n2);
        map.addReadyVM(vm10);

        Model mo = new DefaultModel(map);
        mo.attach(rcMem);
        return mo;
    }

    @Override
    public boolean run() {

        Model mo = makeModel();

        //Change the duration evaluator for MigrateVM action
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        DurationEvaluators dev = cra.getDurationEvaluators();
        dev.register(MigrateVM.class, new LinearToAResourceDuration("mem", 2, 3));

        //Set some attributes
        Attributes attrs = mo.getAttributes();
        for (int vm : mo.getMapping().getAllVMs()) {
            long i = vm.getLeastSignificantBits();
            attrs.put(vm, "template", i % 2 == 0 ? "small" : "large");
            attrs.put(vm, "clone", true);
            attrs.put(vm, "forge", i % 2 == 0 ? 2 : 6);
        }
        List<SatConstraint> cstrs = new ArrayList<>();

        //No more than 5 VMs per node
        cstrs.add(new SingleRunningCapacity(mo.getMapping().getAllNodes(), 5));

        //vm1 and vm10 on the same node
        cstrs.add(new Gather(new HashSet<>(Arrays.asList(vm1, vm10))));

        //(vm1, vm2, vm4) and (vm5, vm6, vm8) must not share node
        cstrs.add(new Split(new HashSet<>(Arrays.asList(g1, g2))));

        //vm10 must be running
        cstrs.add(new Running(Collections.singleton(vm10)));

        try {
            cra.doOptimize(true);
            ReconfigurationPlan plan = cra.solve(mo, cstrs);
            System.out.println(plan);
        } catch (SolverException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
        return true;
    }
}
