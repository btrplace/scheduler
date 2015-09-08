package org.btrplace.safeplace.runner;

import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fhermeni on 07/09/2015.
 */
public class Metrics {

    long fuzzing;

    long validation;

    long testing;

    Map<String, Long> reductions;

    public Metrics() {
        reductions = new HashMap<>();
    }

    public static Metrics sum(Metrics... metrics) {
        return sum(Arrays.asList(metrics));
    }

    public static Metrics sum(List<Metrics> l) {
        Metrics r = new Metrics();
        for (Metrics x : l) {
            r.fuzzing += x.fuzzing;
            r.validation += x.validation;
            r.testing += x.testing;
            for (Map.Entry<String, Long> e : x.reductions.entrySet()) {
                long v = e.getValue();
                if (r.reductions.containsKey(e.getKey())) {
                    v += r.reductions.get(e.getKey());
                }
                r.reductions.put(e.getKey(), v);
            }
        }
        return r;
    }

    public long duration() {
        long d = fuzzing + validation + testing;
        for (Map.Entry<String, Long> e : reductions.entrySet()) {
            d += e.getValue();
        }
        return d;
    }
}
