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

package org.btrplace.safeplace.testing;

import org.btrplace.model.Model;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.spec.term.UserVar;
import org.btrplace.safeplace.spec.type.IntType;
import org.btrplace.safeplace.spec.type.NodeType;
import org.btrplace.safeplace.spec.type.SetType;
import org.btrplace.safeplace.spec.type.VMType;
import org.btrplace.safeplace.testing.fuzzer.Matches;
import org.btrplace.safeplace.testing.fuzzer.ReconfigurationPlanFuzzer;
import org.btrplace.safeplace.testing.fuzzer.decorators.FuzzerDecorator;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class DefaultTestCaseFuzzer implements Supplier<TestCase>, TestCaseFuzzer {

    private Random rnd;

    private ReconfigurationPlanFuzzer fuzzer;

    private Matches predicates;

    private Map<String, Domain> doms;

    private Constraint cstr;

    private Set<Restriction> restrictions;

    private long fuzzingDuration = 0;

    private long lastValidationDuration = 0;

    private int iterations;

    public DefaultTestCaseFuzzer(ReconfigurationPlanFuzzer f) {
        rnd = new Random();
        fuzzer = f;
        doms = new HashMap<>();
        restrictions = EnumSet.allOf(Restriction.class);
        predicates = new Matches();
    }

    @Override
    public long lastFuzzingDuration() {
        return fuzzingDuration;
    }

    @Override
    public long lastValidationDuration() {
        return lastValidationDuration;
    }

    @Override
    public int lastFuzzingIterations() {return iterations;}

    private Domain domain(UserVar v, Model mo) {
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
            return new DefaultDomain<>("nodes", NodeType.getInstance(), new ArrayList<>(mo.getMapping().getAllNodes()));
        } else if (back.enclosingType().equals(VMType.getInstance())) {
            return new DefaultDomain<>("vms", VMType.getInstance(), new ArrayList<>(mo.getMapping().getAllVMs()));
        }
        throw new IllegalArgumentException("No domain value attached to argument '" + v.label() + "'");
    }

    @Override
    public TestCase get() {
        if (constraint() == null) {
            throw new IllegalArgumentException("No constraint to test");
        }
        ReconfigurationPlan p;
        fuzzingDuration = -System.currentTimeMillis();
        lastValidationDuration = 0;
        iterations = 0;
        TestCase tc;
        do {
            lastValidationDuration  += predicates.lastDuration();
            p = fuzzer.get();
            tc = new TestCase(fuzzer.toInstance(p),p, cstr);
            iterations++;
        } while (!predicates.test(tc));
        lastValidationDuration  += predicates.lastDuration();

        List<Constant> specArgs = new ArrayList<>();
        for (UserVar v : cstr.args()) {
            String lbl = v.label();
            Domain d = domain(v, p.getOrigin());
            Object o = v.pick(d);
            tc.with(lbl, o);
            specArgs.add(new Constant(o, v.type()));

        }
        tc.args(specArgs);
        if (cstr.isSatConstraint()) {
            SatConstraint impl = cstr.instantiate(specArgs.stream().map(c -> c.eval(null)).collect(Collectors.toList()));
                tc.instance().getSatConstraints().add(impl);
                fuzzRestriction(impl);
                tc.impl(impl);
        }
        fuzzingDuration += System.currentTimeMillis();
        return tc;
    }

    private void fuzzRestriction(SatConstraint impl) {
        boolean continuous = impl.isContinuous();
        int possibles = 1;
        if (impl.setContinuous(!impl.isContinuous())) {
            possibles++;
        }
        //restore
        impl.setContinuous(continuous);


        if (possibles == 2) {
            if (restrictions.size() == 2) {
                //Both possibles and don't care
                impl.setContinuous(rnd.nextBoolean());
                return;
            } else {
                //Force the right one
                if (restrictions.contains(Restriction.continuous)) {
                    impl.setContinuous(true);
                } else {
                    impl.setContinuous(false);
                }
                return;
            }
        }
        //Only 1 possible, go for it if allowed
        if (!continuous && !restrictions.contains(Restriction.discrete)) {
            throw new IllegalArgumentException(cstr + " implementation cannot be discrete");
        }

        if (continuous && !restrictions.contains(Restriction.continuous)) {
            throw new IllegalArgumentException(cstr + " implementation cannot be continuous");
        }
    }

    @Override
    public TestCaseFuzzer with(String var, int val) {
        Domain d = new DefaultDomain<>("int", IntType.getInstance(), Collections.singletonList(val));
        return with(var, d);
    }

    @Override
    public TestCaseFuzzer with(String var, int min, int max) {
        List<Integer> s = new ArrayList<>();
        for (int m = min; m <= max; m++) {
            s.add(m);
        }
        return with(var, new DefaultDomain<>("int", IntType.getInstance(), s));
    }

    @Override
    public TestCaseFuzzer with(String var, int [] vals) {
        List<Integer> s = new ArrayList(Arrays.asList(vals));
        return with(var, new DefaultDomain<>("int", IntType.getInstance(), s));
    }

    @Override
    public TestCaseFuzzer with(String var, String val) {
        List<String> s = new ArrayList<>(Collections.singleton(val));
        return with(var, new DefaultDomain<>("int", IntType.getInstance(), s));
    }

    @Override
    public TestCaseFuzzer with(String var,  String[] vals) {
        Domain d = new DefaultDomain<>("int", IntType.getInstance(), Arrays.asList(vals));
        doms.put(var, d);
        return this;
    }

    @Override
    public TestCaseFuzzer with(String var,  Domain d) {
        doms.put(var, d);
        return this;
    }

    @Override
    public TestCaseFuzzer validating(Constraint c, Tester t) {
        predicates.setTester(t);
        predicates.with(c);
        return this;
    }

    @Override
    public TestCaseFuzzer restriction(Set<Restriction> domain) {
        restrictions = domain;
        return this;
    }

    @Override
    public ReconfigurationPlanFuzzer vms(int nb) {
        return fuzzer.vms(nb);
    }

    @Override
    public ReconfigurationPlanFuzzer nodes(int nb) {
        return fuzzer.nodes(nb);
    }

    @Override
    public ReconfigurationPlanFuzzer with(FuzzerDecorator f) {
        return fuzzer.with(f);
    }

    @Override
    public ReconfigurationPlanFuzzer srcOffNodes(double ratio) {
        return fuzzer.srcOffNodes(ratio);
    }

    @Override
    public ReconfigurationPlanFuzzer dstOffNodes(double ratio) {
        return fuzzer.dstOffNodes(ratio);
    }

    @Override
    public ReconfigurationPlanFuzzer srcVMs(double ready, double running, double sleeping) {
        return fuzzer.srcVMs(ready, running, sleeping);
    }

    @Override
    public ReconfigurationPlanFuzzer dstVMs(double ready, double running, double sleeping) {
        return fuzzer.dstVMs(ready, running, sleeping);
    }

    @Override
    public ReconfigurationPlanFuzzer durations(int min, int max) {
        return fuzzer.durations(min, max);
    }

    @Override
    public TestCaseFuzzer constraint(Constraint cstr) {
        this.cstr = cstr;
        return this;
    }

    @Override
    public Constraint constraint() {
        return cstr;
    }

    @Override
    public DefaultTestCaseFuzzer supportedConstraints(List<Constraint> cstrs) {
        return this;
    }
}
