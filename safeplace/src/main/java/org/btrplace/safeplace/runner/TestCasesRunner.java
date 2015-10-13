package org.btrplace.safeplace.runner;

import org.btrplace.safeplace.Constraint;
import org.btrplace.safeplace.fuzzer.Fuzzer;
import org.btrplace.safeplace.reducer.Reducer;
import org.btrplace.safeplace.runner.limit.Limit;
import org.btrplace.safeplace.verification.Verifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by fhermeni on 07/09/2015.
 */
public class TestCasesRunner {

    private int slaves;

    private String label;

    private Constraint cstr;

    private List<Limit> limits;

    private Verifier verif;

    private int to;

    private List<Constraint> preconditions;

    private List<Reducer> reducers;

    public TestCasesRunner(List<Constraint> pre, String lbl, Constraint c) {
        label = lbl;
        cstr = c;
        limits = new ArrayList<>();
        preconditions = pre;
        slaves = Runtime.getRuntime().availableProcessors();
        reducers = new ArrayList<>();
    }

    public TestCasesRunner slaves(int nb) {
        slaves = nb;
        return this;
    }

    public int slaves() {
        return slaves;
    }

    public List<TestCaseResult> run(Fuzzer f) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(slaves);

        List<TestCaseResult> results = new ArrayList<>();
        List<SlaveRunner> runners = new ArrayList<>();
        for (int i = 0; i < slaves; i++) {
            runners.add(new SlaveRunner(f.clone(), this));
        }

        for (Future<List<TestCaseResult>> res : executor.invokeAll(runners, to, TimeUnit.SECONDS)) {
            if (res.isDone()) {
                results.addAll(res.get());
            }
        }
        return results;
    }

    public List<Limit> limits() {
        return Collections.unmodifiableList(limits);
    }

    public Constraint constraint() {
        return cstr;
    }

    public String label() {
        return label;
    }

    public List<Constraint> preconditions() {
        return Collections.unmodifiableList(preconditions);
    }

    public TestCasesRunner limit(Limit l) {
        limits.add(l);
        return this;
    }

    public TestCasesRunner timeout(int to) {
        this.to = to;
        return this;
    }

    public TestCasesRunner verifier(Verifier v) {
        verif = v;
        return this;
    }

    public Verifier verifier() {
        return verif;
    }

    public List<Reducer> reducers() {
        return Collections.unmodifiableList(reducers);
    }

    public TestCasesRunner reducer(Reducer r) {
        reducers.add(r);
        return this;
    }
}
