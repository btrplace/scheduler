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

import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Metrics {

    private long fuzzing;

    private long validation;

    private long testing;

    private long fuzzingIterations;

    private Map<String, Long> reductions;

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

    public Metrics plus(Metrics m) {
        Metrics x = new Metrics();
        x.fuzzing = this.fuzzing + m.fuzzing;
        x.testing = this.testing + m.testing;
        x.validation = this.validation + m.validation;
        x.fuzzingIterations = this.fuzzingIterations + m.fuzzingIterations;
        return x;
    }
    public static Metrics averages(List<Metrics> l) {
        Metrics r = sum(l);
        r.fuzzing /= l.size();
        r.validation /= l.size();
        r.testing /= l.size();
        return r;
    }

    public long duration() {
        long d = fuzzing + validation + testing;
        for (Map.Entry<String, Long> e : reductions.entrySet()) {
            d += e.getValue();
        }
        return d;
    }

    @Override
    public String toString() {
        return "Fuzzing: " + fuzzing + "ms; Validation: " + validation + " ms; Fuzzing iterations: " + fuzzingIterations + "; Testing: " + testing + "ms";
    }

    public long fuzzing() {
        return fuzzing;
    }

    public long validation() {
        return validation;
    }

    public long testing() {
        return testing;
    }

    public long fuzzingIterations() {
        return fuzzingIterations;
    }

    public void fuzzing(long fuzzing) {
        this.fuzzing = fuzzing;
    }

    public void validation(long validation) {
        this.validation = validation;
    }

    public void testing(long testing) {
        this.testing = testing;
    }

    public void fuzzingIterations(long fuzzingIterations) {
        this.fuzzingIterations = fuzzingIterations;
    }
}
