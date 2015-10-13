package org.btrplace.safeplace.fuzzer;

import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.safeplace.Constraint;
import org.btrplace.safeplace.verification.spec.Context;
import org.btrplace.safeplace.verification.spec.Domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhermeni on 25/07/2015.
 */
public class FuzzerImpl implements Fuzzer {

    private List<ModelViewFuzzer> views;

    private ModelGenerator moGen;

    private ReconfigurationPlanGenerator pGen;

    private ParamsFuzzer argsGen;


    public FuzzerImpl() {
        views = new ArrayList<>();
        moGen = new ModelGenerator();
        pGen = new ReconfigurationPlanGenerator();
        argsGen = new ParamsFuzzer();
    }

    @Override
    public TestCase fuzz(String lbl, Constraint c) {

        //the reconfiguration plan with the durations
        ReconfigurationPlan p = pGen.build(moGen.build());

        Context spec = new Context(p.getOrigin());

        //the views
        for (ModelViewFuzzer f : views) {
            f.decorate(p);
        }

        return new TestCase(lbl, c, argsGen.build(c, spec), p, argsGen.continuous(c));
    }

    public FuzzerImpl add(ModelViewFuzzer v) {
        views.add(v);
        return this;
    }

    public FuzzerImpl dom(Domain v) {
        return this;
    }

    public FuzzerImpl nodes(int n) {
        moGen.nodes(n);
        return this;
    }

    public FuzzerImpl vms(int n) {
        moGen.vms(n);
        return this;
    }

    public FuzzerImpl actionBounds(int min, int max) {
        pGen.min(min).max(max);
        return this;
    }

    public FuzzerImpl continuous(boolean b) {
        return this;
    }

    public FuzzerImpl discrete(boolean b) {
        return this;
    }

    @Override
    public Fuzzer clone() {
        FuzzerImpl cpy = new FuzzerImpl();
        cpy.moGen.nodes(moGen.nodes());
        cpy.moGen.vms(moGen.vms());
        cpy.pGen.min(pGen.min());
        cpy.pGen.max(pGen.max());
        cpy.argsGen.discrete(argsGen.discrete());
        cpy.argsGen.continuous(argsGen.continuous());
        return cpy;
    }
}
