/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco.constraint.migration;

import org.btrplace.json.JSON;
import org.btrplace.model.Instance;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.MinMigrations;
import org.btrplace.model.constraint.OptConstraint;
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
import java.util.Stack;

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
        System.out.println(p);
        System.out.println(s.getStatistics());
        Assert.assertEquals(p.getActions().stream().filter(x -> x instanceof MigrateVM).count(), 1);
    }

    @Test
    public void testFoo() {
        String root = "/Users/fhermeni/Documents/Research/Projects/BtrPlace/nutanix/instances";
        List<OptConstraint> objs = Arrays.asList(new MinMTTR(), new MinMigrations());
        boolean verbose = false;

        System.out.println("  migs\tmttr");
        for (int idx = 2; idx <= 7; idx++) {
            String path = root + "/instance-" + idx + ".json";
            if (verbose) {
                System.out.println("--- " + idx + " --- ");
            }

            Stack<Long> res = new Stack<>();
            for (OptConstraint o : objs) {
                if (verbose) {
                    System.out.println("\t" + o);
                }
                Instance i = JSON.readInstance(new File(path));
                i = new Instance(i.getModel(), i.getSatConstraints(), o);
                ChocoScheduler s = new DefaultChocoScheduler();
                s.doOptimize(true);
                s.setTimeLimit(20);
                ReconfigurationPlan p = s.solve(i);
                Assert.assertNotNull(p);
                res.add(p.getActions().stream().filter(x -> x instanceof MigrateVM).count());
                if (verbose) {
                    System.out.println(s.getStatistics());
                    System.out.println(p);
                }
            }
            if (!verbose) {
                System.out.printf("%d:\t%d\t%d%n", idx, res.pop(), res.pop());
            }

        }
    }
}