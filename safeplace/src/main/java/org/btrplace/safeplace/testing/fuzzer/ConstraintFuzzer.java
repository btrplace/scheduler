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

package org.btrplace.safeplace.testing.fuzzer;

import org.btrplace.model.Model;
import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.spec.term.UserVar;
import org.btrplace.safeplace.spec.type.*;
import org.btrplace.safeplace.testing.fuzzer.domain.ConstantDomain;
import org.btrplace.safeplace.testing.fuzzer.domain.Domain;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Fabien Hermenier
 */
public class ConstraintFuzzer {

    private Map<String, Domain> doms;

    private Constraint toTest;

    public ConstraintFuzzer(String name, List<Constraint> cores, List<Constraint> sides) {
        doms = new HashMap<>();

        String lower = name.toLowerCase();
        Optional<Constraint> o = Stream.concat(cores.stream(), sides.stream())
                .filter(c -> lower.equalsIgnoreCase(c.id()))
                .findFirst();

        if (!o.isPresent()) {
            throw new IllegalArgumentException("No specification for " + name);
        }
        toTest = o.get();

    }

    public Domain domain(UserVar v, Model mo) {
        Domain d = doms.get(v.label());
        if (d != null) {
            return d;
        }

        //Default domains
        if (!(v.getBackend().type() instanceof SetType)) {
            return null;
        }
        SetType back = (SetType) v.getBackend().type();
        if (back.enclosingType().equals(NodeType.getInstance())) {
            return new ConstantDomain<>("nodes", NodeType.getInstance(), new ArrayList<>(mo.getMapping().getAllNodes()));
        } else if (back.enclosingType().equals(VMType.getInstance())) {
            return new ConstantDomain<>("vms", VMType.getInstance(), new ArrayList<>(mo.getMapping().getAllVMs()));
        }
        throw new IllegalArgumentException("No domain value attached to argument '" + v.label() + "'");
    }

    public Constraint toTest() {
        return toTest;
    }
    public ConstraintFuzzer with(String var, int val) {
        Domain d = new ConstantDomain<>("int", IntType.getInstance(), Collections.singletonList(val));
        return with(var, d);
    }

    public ConstraintFuzzer with(String var, int min, int max) {
        List<Integer> s = new ArrayList<>();
        for (int m = min; m <= max; m++) {
            s.add(m);
        }
        return with(var, new ConstantDomain<>("int", IntType.getInstance(), s));
    }

    public ConstraintFuzzer with(String var, int [] vals) {
        List<Integer> s = new ArrayList(Arrays.asList(vals));
        return with(var, new ConstantDomain<>("int", IntType.getInstance(), s));
    }

    public ConstraintFuzzer with(String var, String val) {
        List<String> s = new ArrayList<>(Collections.singleton(val));
        return with(var, new ConstantDomain<>("string", StringType.getInstance(), s));
    }

    public ConstraintFuzzer with(String var,  String[] vals) {
        Domain d = new ConstantDomain<>("string", StringType.getInstance(), Arrays.asList(vals));
        doms.put(var, d);
        return this;
    }

    public ConstraintFuzzer with(String var,  Domain d) {
        doms.put(var, d);
        return this;
    }
}
