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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Fabien Hermenier
 */
public class Check {

    @Test
    public void run() {
        FuzzTesting tester = new FuzzTesting();
        InstanceGenerator source = new InstanceGenerator();
        List<Result> l = Stream.generate(source).limit(10000)
                .map(tester::crashTest).filter(r -> !r.succeed())
                .collect(Collectors.toList());
        for (Result r : l) {
            System.out.println("---");
            System.out.println(r);
        }
        Assert.assertEquals(l.size(), 0);
    }
}
