/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint.migration;

import org.btrplace.json.JSON;
import org.btrplace.model.*;
import org.btrplace.model.constraint.*;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.model.view.network.Network;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class CMinMigrationsTest {

    @Test
    public void testNtnx() {
        String root = "src/test/resources/min-migrations.json";
        Instance i = JSON.readInstance(new File(root));
        i = new Instance(i.getModel(), i.getSatConstraints(), new MinMigrations());
        ChocoScheduler s = new DefaultChocoScheduler();
        s.doOptimize(true);
        ReconfigurationPlan p = s.solve(i);
        Assert.assertNotNull(p);
        System.out.println(s.getStatistics());
        System.out.println(p);
        Assert.assertEquals(p.getActions().stream().filter(x -> x instanceof MigrateVM).count(), 1);
        Assert.assertEquals(p.getDuration(), 3);
    }


    @Test
    public void simpleTest() {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();

        VM vm0 = mo.newVM();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();

        mo.getMapping().on(n0, n1)
                .run(n0, vm0, vm1, vm2);

        ShareableResource mem = new ShareableResource("mem", 5, 1);

        //cpu dimension to create the violation
        ShareableResource cpu = new ShareableResource("cpu", 5, 1);
        mem.setConsumption(vm0, 3); //VM0 as the big VM

        mo.attach(cpu);
        mo.attach(mem);
        List<SatConstraint> cstrs = new ArrayList<>();
        //The 3 VMs no longer feet on n0
        cstrs.add(new Preserve(vm0, "cpu", 5));

        ChocoScheduler s = new DefaultChocoScheduler();
        s.doOptimize(true);
        ReconfigurationPlan p = s.solve(mo, cstrs, new MinMigrations());
        System.out.println(s.getStatistics());
        System.out.println(p);
        Assert.assertEquals(p.getActions().size(), 1); //VM0 to n1
        Assert.assertEquals(p.getResult().getMapping().getVMLocation(vm0), n1); //VM0 to n1
    }

    /**
     * Issue #137.
     * ShutdownableNode.isOnline() is not instantiated at the end of the problem
     */
    @Test
    public void testWithFreeNodes() {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        VM v1 = mo.newVM();
        VM v2 = mo.newVM();
        DefaultChocoScheduler s = new DefaultChocoScheduler();
        mo.getMapping().on(n0, n1, n2).run(n0, v1, v2);
        Instance i = new Instance(mo, Arrays.asList(new Spread(mo.getMapping().getAllVMs(), false)), new MinMigrations());
        ReconfigurationPlan p = s.solve(i);
        Assert.assertNotNull(p);
    }


    public static void main(String[] args) {
        String root = "/Users/fabien.hermenier/Documents/BtrPlace/nutanix/instances";
        List<OptConstraint> objs = Arrays.asList(/*new MinMTTR(),*/ new MinMigrations());
        boolean verbose = true;

        for (OptConstraint o : objs) {
            System.out.print(" " + o);
        }
        System.out.println();
        for (int idx = 1; idx <= 256; idx += 5) {
            String path = root + "/lazan/lazan-" + idx + ".json.gz";

            if (verbose) {
                System.out.println("--- " + idx + " --- ");
            } else {
                System.out.print(idx);
            }

            List<Long> res = new ArrayList<>();
            for (OptConstraint o : objs) {
                if (verbose) {
                    System.out.println("\t" + o);
                }
                Instance i = JSON.readInstance(new File(path));
                if (Network.get(i.getModel()) != null) {
                    i.getModel().detach(Network.get(i.getModel()));
                }
                i = new Instance(i.getModel(), i.getSatConstraints(), o);
                ChocoScheduler s = new DefaultChocoScheduler();
                s.doOptimize(false);
                s.setTimeLimit(30);
                s.doRepair(false);
                ReconfigurationPlan p = s.solve(i);
                Assert.assertNotNull(p);
                res.add(p.getActions().stream().filter(x -> x instanceof MigrateVM).mapToLong(x -> x.getEnd() - x.getStart()).sum());
                if (verbose) {
                    System.out.println(s.getStatistics());
                }
            }
            if (!verbose) {
                for (Long l : res) {
                    System.out.print("\t" + l);
                }
                System.out.println();
            }
        }
    }
}
