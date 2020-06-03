/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing.fuzzer;

import org.btrplace.json.JSONConverterException;
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
import org.btrplace.safeplace.testing.TestCase;
import org.btrplace.safeplace.testing.Tester;
import org.btrplace.safeplace.testing.fuzzer.decorators.FuzzerDecorator;
import org.btrplace.safeplace.testing.fuzzer.domain.ConstantDomain;
import org.btrplace.safeplace.testing.fuzzer.domain.Domain;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of a fuzzer.
 * @author Fabien Hermenier
 */
public class DefaultFuzzer implements ConfigurableFuzzer {

  private final Random rnd;

  private final ReconfigurationPlanFuzzer fuzzer;

  private final Validator predicates;

  private final Map<String, Domain<?>> doms;

  private final Constraint cstr;

    private Set<Restriction> restrictions;

    private long fuzzingDuration = 0;

    private long lastValidationDuration = 0;

    private int iterations;

    private Writer writer;

    /**
     * Make a new fuzzer.
     *
     * @param t      the tester to use to validate the test case
     * @param toTest the constraint to test inside the test cases
     * @param pre    the constraint to use to validate the generate test case
     */
    public DefaultFuzzer(Tester t, Constraint toTest, List<Constraint> pre) {
      rnd = new Random(0);
      fuzzer = new ReconfigurationPlanFuzzer(rnd);
        doms = new HashMap<>();
        restrictions = EnumSet.allOf(Restriction.class);
        predicates = new Validator(t, pre);
        cstr = toTest;
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

    private Domain<?> domain(UserVar<?> v, Model mo) {
        Domain<?> d = doms.get(v.label());
        if (d != null) {
            return d;
        }

        //Default domains
        if (!(v.getBackend().type() instanceof SetType)) {
            return null;
        }
        SetType back = (SetType) v.getBackend().type();
        if (back.enclosingType().equals(NodeType.getInstance())) {
          return new ConstantDomain<>(rnd, "nodes", NodeType.getInstance(), new ArrayList<>(mo.getMapping().getAllNodes()));
        } else if (back.enclosingType().equals(VMType.getInstance())) {
          return new ConstantDomain<>(rnd, "vms", VMType.getInstance(), new ArrayList<>(mo.getMapping().getAllVMs()));
        }
        throw new IllegalArgumentException("No domain value attached to argument '" + v.label() + "'");
    }

    @Override
    public TestCase get() {

        ReconfigurationPlan p;
        fuzzingDuration = -System.currentTimeMillis();
        lastValidationDuration = 0;
        iterations = 0;
        TestCase tc;
        do {
            lastValidationDuration  += predicates.lastDuration();
            p = fuzzer.get();
            tc = new TestCase(InstanceConverter.toInstance(p), p, cstr);
            iterations++;
          if (iterations > 10000) {
            // We abandon the fuzzing stage. Too hard to get a validation.
            return null;
          }
        } while (!predicates.test(tc));
        lastValidationDuration  += predicates.lastDuration();

        List<Constant> specArgs = new ArrayList<>();
        for (UserVar<?> v : cstr.args()) {
            Domain<?> d = domain(v, p.getOrigin());
            Object o = v.pick(d);
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

        store(tc);
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
            }
            //Force the right one
          impl.setContinuous(restrictions.contains(Restriction.CONTINUOUS));
            return;
        }
        //Only 1 possible, go for it if allowed
        if (!continuous && !restrictions.contains(Restriction.DISCRETE)) {
          throw new IllegalArgumentException(cstr + " implementation cannot be discrete");
        }

        if (continuous && !restrictions.contains(Restriction.CONTINUOUS)) {
          throw new IllegalArgumentException(cstr + " implementation cannot be continuous");
        }
    }

    @Override
    public ConfigurableFuzzer with(String var, int val) {
      Domain<?> d = new ConstantDomain<>(rnd, "int", IntType.getInstance(), Collections.singletonList(val));
        return with(var, d);
    }

    @Override
    public ConfigurableFuzzer with(String var, int min, int max) {
        List<Integer> s = new ArrayList<>();
        for (int m = min; m <= max; m++) {
            s.add(m);
        }
      return with(var, new ConstantDomain<>(rnd, "int", IntType.getInstance(), s));
    }

    @Override
    public ConfigurableFuzzer with(String var, int[] vals) {
      List<Integer> s = Arrays.stream(vals).boxed().collect(Collectors.toList());
      return with(var, new ConstantDomain<>(rnd, "int", IntType.getInstance(), s));
    }

    @Override
    public ConfigurableFuzzer with(String arg, String val) {
        List<String> s = new ArrayList<>(Collections.singleton(val));
      return with(arg, new ConstantDomain<>(rnd, "int", IntType.getInstance(), s));
    }

    @Override
    public ConfigurableFuzzer with(String arg, String[] vals) {
      Domain<?> d = new ConstantDomain<>(rnd, "int", IntType.getInstance(), Arrays.asList(vals));
        doms.put(arg, d);
        return this;
    }

    @Override
    public ConfigurableFuzzer with(String arg, Domain d) {
        doms.put(arg, d);
        return this;
    }

    @Override
    public ConfigurableFuzzer restriction(Set<Restriction> domain) {
        restrictions = domain;
        return this;
    }

    private void store(TestCase tc) {
        if (writer == null) {
            return;
        }
        try {
            writer.write(tc.toJSON());
            writer.flush();
        } catch (IOException | JSONConverterException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public DefaultFuzzer save(Writer w) {
        writer = w;
        return this;
    }

    @Override
    public DefaultFuzzer save(String path) {
        try {
            return save(Files.newBufferedWriter(Paths.get(path), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    //Delegation to the plan fuzzer
    @Override
    public ConfigurableFuzzer vms(int nb) {
        fuzzer.vms(nb);
        return this;
    }

    @Override
    public ConfigurableFuzzer nodes(int nb) {
        fuzzer.nodes(nb);
        return this;
    }

    @Override
    public ConfigurableFuzzer with(FuzzerDecorator f) {
        fuzzer.with(f);
        return this;
    }

    @Override
    public ConfigurableFuzzer srcOffNodes(double ratio) {
        fuzzer.srcOffNodes(ratio);
        return this;
    }

    @Override
    public ConfigurableFuzzer dstOffNodes(double ratio) {
        fuzzer.dstOffNodes(ratio);
        return this;
    }

    @Override
    public ConfigurableFuzzer srcVMs(int ready, int running, int sleeping) {
        fuzzer.srcVMs(ready, running, sleeping);
        return this;
    }

    @Override
    public ConfigurableFuzzer dstVMs(int ready, int running, int sleeping) {
        fuzzer.dstVMs(ready, running, sleeping);
        return this;
    }

    @Override
    public ConfigurableFuzzer durations(int min, int max) {
        fuzzer.durations(min, max);
        return this;
    }

}
