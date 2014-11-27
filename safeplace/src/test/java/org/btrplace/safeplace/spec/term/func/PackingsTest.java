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

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.safeplace.verification.spec.SpecModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class PackingsTest {

    @Test
    public void test() {
        Set<Integer> s = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            s.add(i);
        }
        //System.out.println(AllTuplesGenerator.allSubsets(Integer.class, s));
        List args = Arrays.asList(s);
        Packings p = new Packings();
        Set<Set<Set<Object>>> res = p.eval(new SpecModel(), args);
        //System.out.println(res);
        for (Set<Set<Object>> x : res) {
            System.out.println(x);
        }
        Assert.assertEquals(res.size(), 14);
        /*
         [[0]]
         [[1]]
         [[2]]
         [[0,1]]
         [[0,2]]
         [[1,2]]
         [[1,2,3]]
         [[0,1],[2]]
         [[0,2],[1]]
         [[1,2],[0]]
         [[0],[1]]
         [[0],[2]]
         [[1],[2]]
         [[1],[2],[3]]

        0
        0 1
        0 1 2
        0,1
        0,1 2
        0,2
        0,2 1
        0,1,2
        1
        1 0
        1 0 2
        1 2
        1 2 0
        1,2
        1,2 0
        1,2,0
        2
        2 0
         */
    }
}
