package org.btrplace.safeplace.runner.limit;

import org.btrplace.safeplace.runner.TestCaseResult;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fhermeni on 08/09/2015.
 */
public class MaxTests implements Limit {

    private AtomicInteger counter;

    public MaxTests(int m) {
        counter = new AtomicInteger(m);
    }

    @Override
    synchronized public boolean pass(TestCaseResult o) {
        return counter.getAndDecrement() > 0;
    }
}
