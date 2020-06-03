/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

  private final List<T> dom;

  private final Type t;
  private final Random rnd;

  private final String name;

  public ConstantDomain(String name, Type t, List<T> dom) {
    this(new Random(), name, t, dom);
  }

  public ConstantDomain(Random rnd, String name, Type t, List<T> dom) {
    this.rnd = rnd;
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
