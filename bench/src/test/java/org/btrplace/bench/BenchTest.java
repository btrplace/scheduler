/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.bench;

import org.btrplace.json.JSON;
import org.btrplace.model.*;
import org.btrplace.model.constraint.*;
import org.btrplace.plan.ReconfigurationPlan;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Fabien Hermenier
 */
public class BenchTest {

    public static Instance instance() {
        Model mo = new DefaultModel();
        VM v1 = mo.newVM();
        VM v2 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        mo.getMapping().on(n1, n2, n3).run(n1, v1).run(n2, v2).off(n3);
        Set<VM> s = new HashSet<>();
        s.add(v1);
        s.add(v2);
        List<SatConstraint> cstrs = Arrays.asList(
                new Spread(s, true),
                new Fence(v1, n2),
                new Offline(n1)
        );
        return new Instance(mo, cstrs, new MinMTTR());
    }

    public static File store(Instance i) throws Exception {
        File f = File.createTempFile("foo", ".gz");
        f.deleteOnExit();
        JSON.write(i, f);
        return f;
    }

    /**
     * Create an instance file, solve it using the bench
     */
    @Test
    public void testSingle() throws Exception {
        Instance i = instance();
        File f = store(i);
        Bench.main(new String[]{
                "-i", f.getAbsolutePath(),
                "-v", "3"
        });
    }

    /**
     * Create a liste, solve each instance. Check for the CSV file
     *
     * @throws Exception
     */
    @Test
    public void testList() throws Exception {
        List<String> files = new ArrayList<>();
        for (int x = 0; x < 5; x++) {
            Instance i = instance();
            File f = store(i);
            files.add(f.getPath());
        }
        //Instance list
        File list = File.createTempFile("foo", "");
        list.deleteOnExit();

        //Output directory
        Path output = Files.createTempDirectory("instances");

        Files.write(list.toPath(), files, UTF_8);
        Bench.main(new String[]{
                "-l", list.getAbsolutePath(),
                "-o", output.toString(),
                "-v", "1"
        });

        //Read the output CSV file
        File csv = new File(output.toString() + File.separator + Bench.SCHEDULER_STATS);
        Assert.assertTrue(csv.isFile());

        for (String line : Files.readAllLines(csv.toPath(), UTF_8)) {
            String file = line.split(";")[0];
            File plan = new File(output.toString() + File.separator + file + ".gz");

            System.out.println(plan.getAbsolutePath());
            Assert.assertTrue(plan.isFile());
            ReconfigurationPlan p = JSON.readReconfigurationPlan(plan);
            System.out.println(p);
        }
    }

    //    @Test
    public void testAllocation() throws Exception {
        String base = "bench/src/test/resources/std-perf/";
        String[] ids = {
                "li3.gz",
                "li4.gz",
                "li5.gz",
                "li6.gz",
                "nr3.gz",
                "nr4.gz",
                "nr5.gz",
                "nr6.gz",
                "issue-100.gz",
        };
        for (String id : ids) {
            System.out.println("------- " + id + " ----------");
            Bench.main(new String[]{
                    "-i", base + id,
                    "--repair",

            });
        }

    }
}
