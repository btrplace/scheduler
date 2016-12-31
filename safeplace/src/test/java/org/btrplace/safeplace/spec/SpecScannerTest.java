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

package org.btrplace.safeplace.spec;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class SpecScannerTest {

    @Test
    public void test() throws Exception {
        SpecScanner sc = new SpecScanner();
        List<Constraint> l = sc.scan();
        Assert.assertEquals(l.size(), 27);
        long from = System.currentTimeMillis();
        System.out.println(l.stream().map(Constraint::pretty).collect(Collectors.joining("\n")));
        System.out.println(System.currentTimeMillis() - from + " ms");
    }
}