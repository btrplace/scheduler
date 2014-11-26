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
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.duration.ConstantActionDuration;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.btrplace.scheduler.choco.duration.LinearToAResourceActionDuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Tutorial about the basic tuning of a model.
 *
 * @author Fabien Hermenier
 * @see <a href="https://github.com/btrplace/scheduler/wiki/Customizing-a-model">btrplace website</a>
 */
public class ModelCustomization implements Example {

    private List<VM> vms = new ArrayList<>();

    private Model makeModel() {
        Model mo = new DefaultModel();
        vms = new ArrayList<>();
        List<Node> ns = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            vms.add(mo.newVM());
        }
        ns.add(mo.newNode());
        ns.add(mo.newNode());
        Mapping map = mo.getMapping();

        map.addOnlineNode(ns.get(0));
        map.addOnlineNode(ns.get(1));

        //32GB by default for the nodes, 1 for the VMs
        ShareableResource rcMem = new ShareableResource("mem", 32, 1);

        for (int i = 0; i < 10; i++) {
            rcMem.setConsumption(vms.get(i), i % 3 + 1);
            //vm0: 1, vm1:2, vm2:3, vm3:1, vm4:2, vm5:3, vm6:1, ...
        }

        map.addRunningVM(vms.get(0), ns.get(0));
        map.addRunningVM(vms.get(1), ns.get(0));
        map.addRunningVM(vms.get(2), ns.get(0));
        map.addRunningVM(vms.get(3), ns.get(0));
        map.addRunningVM(vms.get(4), ns.get(0));
        map.addRunningVM(vms.get(5), ns.get(0));
        map.addRunningVM(vms.get(6), ns.get(1));
        map.addRunningVM(vms.get(7), ns.get(1));
        map.addRunningVM(vms.get(8), ns.get(1));
        map.addReadyVM(vms.get(9));

        mo.attach(rcMem);
        return mo;
    }

    private List<SatConstraint> makeConstraints(Model model) {
        List<SatConstraint> cstrs = new ArrayList<>();

        //No more than 5 VMs per node
        for (Node n : model.getMapping().getAllNodes()) {
            cstrs.add(new RunningCapacity(n, 5));
        }

        //vm1 and vm10 on the same node
        cstrs.add(new Gather(Arrays.asList(vms.get(0), vms.get(9))));

        //(vm1, vm2, vm4) and (vm5, vm6, vm8) must not share node
        Collection<VM> g1 = Arrays.asList(vms.get(0), vms.get(1), vms.get(3));
        Collection<VM> g2 = Arrays.asList(vms.get(4), vms.get(5), vms.get(7));

        cstrs.add(new Split(Arrays.asList(g1, g2)));

        //vm10 must be running
        cstrs.add(new Running(vms.get(9)));
        return cstrs;
    }

    @Override
    public boolean run() {

        Model model = makeModel();
        List<SatConstraint> cstrs = makeConstraints(model);

        //Set attributes for the VMs
        Attributes attrs = model.getAttributes();
        for (VM vm : model.getMapping().getAllVMs()) {
            attrs.put(vm, "template", vm.id() % 2 == 0 ? "small" : "large");
            attrs.put(vm, "clone", true);
            attrs.put(vm, "forge", vm.id() % 2 == 0 ? 2 : 10);
            //forge == 2 && template == small  for vm0, vm2, vm4, vm6, vm8
            //forge == 10 && template == large for vm1, vm3, vm5, vm7, vm9
        }

        //Change the duration evaluator for MigrateVM action
        ChocoScheduler cra = new DefaultChocoScheduler();
        DurationEvaluators dev = cra.getDurationEvaluators();
        dev.register(MigrateVM.class, new LinearToAResourceActionDuration<VM>("mem", 2, 3));
        dev.register(BootVM.class, new ConstantActionDuration(1));
        dev.register(ShutdownVM.class, new ConstantActionDuration(1));

        //Relocate VM4:
        //  using a migration: (2 * mem + 3) = (2 * 2 + 3) = 7 sec.
        //  using a re-instantiation: forge + boot + shutdown = 2 + 1 + 1 = 4 sec.
        //Relocate VM5:
        //  using a migration: (2 * mem + 3) = (2 * 3 + 3) = 9 sec.
        //  using a re-instantiation: forge + boot + shutdown = 10 + 1 + 1 = 12 sec.

        try {
            cra.doOptimize(true);
            ReconfigurationPlan plan = cra.solve(model, cstrs);
            System.out.println(plan);
        } catch (SchedulerException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
        return true;
    }
}
