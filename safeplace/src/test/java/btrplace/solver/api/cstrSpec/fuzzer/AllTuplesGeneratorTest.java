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

package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.solver.api.cstrSpec.util.AllTuplesGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class AllTuplesGeneratorTest {

    @Test
    public void test() {
        List<List<Integer>> l = new ArrayList<>();
        List<Integer> cnt = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            cnt.add(i);
        }
        l.add(cnt);
        l.add(cnt);
        l.add(cnt);
        double nb = 0;
        AllTuplesGenerator<Integer> tg = new AllTuplesGenerator<>(Integer.class, l);
        for (Integer[] t : tg) {
            //System.out.println(t);
            nb++;
        }
        Assert.assertEquals(nb, Math.pow(cnt.size(), l.size()));
    }
}
