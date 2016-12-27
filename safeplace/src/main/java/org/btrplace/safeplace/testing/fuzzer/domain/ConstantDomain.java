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

package org.btrplace.safeplace.testing.fuzzer.domain;

import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class ConstantDomain<T> implements Domain<T> {

    private List<T> dom;

    private Type t;
    private Random rnd = new Random();

    private String name;

    public ConstantDomain(String name, Type t, List<T> dom) {
        this.dom = dom;
        this.t = t;
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Type type() {
        return t;
    }

    @Override
    public List<T> values() {
        return dom;
    }

    @Override
    public T randomValue() {
        return dom.get(rnd.nextInt(dom.size()));
    }

    @Override
    public List<T> randomSubset() {
        return values().stream().filter(i -> rnd.nextInt(3) == 2).collect(Collectors.toList());
    }

    @Override
    public List<List<T>> randomPacking() {
        //Compute a number of sets, between 0 and size
        int s = rnd.nextInt(dom.size() + 1);
        List<List<T>> res = new ArrayList<>(s);
        for (int i = 0; i < s; i++) {
            res.add(new ArrayList<>());
        }
        for (T i : values()) {
            int idx = rnd.nextInt(s + 1);
            if (idx >= 1) {
                res.get(idx - 1).add(i);
            }
        }
        return res;
    }

    @Override
    public List<T> eval(Context mo, Object... args) {
        return dom;
    }
}
