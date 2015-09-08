package org.btrplace.safeplace.runner;

import org.btrplace.safeplace.Constraint;
import org.btrplace.safeplace.fuzzer.Fuzzer;
import org.btrplace.safeplace.fuzzer.TestCase;
import org.btrplace.safeplace.reducer.Reducer;
import org.btrplace.safeplace.runner.limit.Limit;
import org.btrplace.safeplace.verification.CheckerResult;
import org.btrplace.safeplace.verification.Verifier;
import org.btrplace.safeplace.verification.spec.SpecVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by fhermeni on 07/09/2015.
 */
public class SlaveRunner implements Callable<List<TestCaseResult>> {

    private Fuzzer fuzzer;
    private String label;
    private Constraint cstr;
    private SpecVerifier oracle;
    private Verifier verif;
    private List<Limit> limits;
    private List<Constraint> preconditions;
    private List<Reducer> reducers;

    public SlaveRunner(Fuzzer clone, TestCasesRunner r) {
        fuzzer = clone;
        label = r.label();
        cstr = r.constraint();
        oracle = new SpecVerifier(null);
        verif = r.verifier().clone();
        this.limits = r.limits();
        preconditions = r.preconditions();
        reducers = r.reducers();
    }

    @Override
    public List<TestCaseResult> call() throws Exception {
        List<TestCaseResult> results = new ArrayList<>();
        boolean stop = false;
        Metrics m = new Metrics();
        while (!stop) {
            long st = System.currentTimeMillis();
            TestCase tc = fuzzer.fuzz(label, cstr);
            m.fuzzing = since(st);

            st = System.currentTimeMillis();
            while (!valid(tc)) {
                tc = fuzzer.fuzz(label, cstr);
            }
            m.validation = since(st);

            st = System.currentTimeMillis();
            TestCaseResult res = test(tc);
            m.testing = since(st);

            for (Limit l : limits) {
                if (!l.pass(res)) {
                    return results;
                }
            }

            if (res.result() != TestCaseResult.Result.success) {
                for (Reducer r : reducers) {
                    st = System.currentTimeMillis();
                    res = r.reduce(res, oracle, verif);
                    m.reductions.put(r.getClass().getSimpleName(), since(st));
                }
            }
            res.metrics(m);
            results.add(res);
        }
        return results;
    }

    private boolean valid(TestCase tc) {
        //Necessarily against the continuous version
                for (Constraint c : preconditions) {
                    CheckerResult res = oracle.verify(tc);
                    if (!res.getStatus()) {
                        return false;
                    }
                }
                for (Constraint c : preconditions) {
                    CheckerResult res = verif.verify(tc);
                    if (!res.getStatus()) {
                        return false;
                    }
                }
        return true;
    }

    private TestCaseResult test(TestCase tc) {
        TestCaseResult r = new TestCaseResult(tc, oracle.verify(tc), verif.verify(tc));
        return reduce(r);
    }

    private TestCaseResult reduce(TestCaseResult res) {
        if (res.result() == TestCaseResult.Result.success) {
            return res;
        }
        TestCaseResult red = res;
        for (Reducer r : reducers) {
            red = r.reduce(red, oracle, verif);
        }
        return red;
    }

    private static long since(long st) {
        return System.currentTimeMillis() - st;
    }
}
