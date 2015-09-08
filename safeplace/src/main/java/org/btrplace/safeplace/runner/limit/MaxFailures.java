package org.btrplace.safeplace.runner.limit;

import org.btrplace.safeplace.runner.TestCaseResult;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fhermeni on 08/09/2015.
 */
public class MaxFailures implements Limit {

    private AtomicInteger counter;

    public MaxFailures(int m) {
        counter = new AtomicInteger(m);
    }

    @Override
    public boolean pass(TestCaseResult o) {
        if (o.result() != TestCaseResult.Result.success) {
            return counter.decrementAndGet() > 0;
        }
        return true;
    }
}
