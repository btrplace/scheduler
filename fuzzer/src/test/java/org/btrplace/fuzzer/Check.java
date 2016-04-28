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

package org.btrplace.fuzzer;

import org.btrplace.fuzzer.generator.InstanceGenerator;
import org.btrplace.fuzzer.generator.ShareableResourceFuzzer;
import org.btrplace.model.Instance;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Fabien Hermenier
 */
public class Check {

    public static NumberFormat df = new DecimalFormat("#0.00");


    @Test
    public void runCore() {
        FuzzTesting tester = new FuzzTesting();
        InstanceGenerator source = new InstanceGenerator();
        List<Result> l = Stream.generate(source).limit(1000000)
                .map(tester::crashTest).filter(r -> !r.succeed())
                .collect(Collectors.toList());
        for (Result r : l) {
            System.out.println("---");
            System.out.println(r);
        }
        Assert.assertEquals(l.size(), 0);
    }

    @Test
    public void runResource() {
        FuzzTesting tester = new FuzzTesting();
        InstanceGenerator source = new InstanceGenerator().with(new ShareableResourceFuzzer("cpu", 1, 4, 48, 64)).vms(60).nodes(5);

        int count = 100;
        long st = System.currentTimeMillis();
        List<Result> l = Stream.generate(source).limit(count)
                .map(tester::crashTest).filter(r -> !r.succeed())
                .collect(Collectors.toList());
        for (Result r : l) {
            System.out.println("---");
            System.out.println(r);
        }
        long ed = System.currentTimeMillis();
        long duration = ed - st;
        double speed = count / duration;
        System.out.println(count + " test(s); " + (ed - st) + " ms; " + df.format(speed * 1000) + " test(s) / sec.");

        Assert.assertEquals(l.size(), 0);
    }

    @Test
    public void bench() {

        FuzzTesting tester = new FuzzTesting();
        for (int i = 0; i < 10; i++) {
            InstanceGenerator source = new InstanceGenerator()
                    .with(new ShareableResourceFuzzer("cpu", 1, 4, 48, 64));

            long st = System.currentTimeMillis();

            double count = Stream.generate(source)
                    .limit(10000)
                    .map(tester::crashTest)
                    .count();
            long ed = System.currentTimeMillis();
            long duration = ed - st;
            double speed = count / duration;
            System.out.println(count + " test(s); " + (ed - st) + " ms; " + df.format(speed * 1000) + " test(s) / sec.");
        }

    }
}
