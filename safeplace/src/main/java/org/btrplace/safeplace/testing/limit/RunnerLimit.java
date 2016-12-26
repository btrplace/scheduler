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

package org.btrplace.safeplace.testing.limit;

import org.btrplace.safeplace.testing.TestCaseResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class RunnerLimit implements Predicate<TestCaseResult> {

    private List<Predicate<TestCaseResult>> limits;

    public RunnerLimit() {
        limits = new ArrayList<>();
    }

    public RunnerLimit second(int nb) {
        with(new Timeout(nb));
        return this;
    }

    public RunnerLimit tests(int nb) {
        with(new MaxTests(nb));
        return this;
    }

    public RunnerLimit failures(int nb) {
        with(new MaxFailures(nb));
        return this;
    }

    public RunnerLimit success(int nb) {
        with(new MaxSuccess(nb));
        return this;
    }

    public void with(Predicate<TestCaseResult> p) {
        limits.removeIf(r -> r.getClass().equals(p.getClass()));
        limits.add(p);
    }
    public RunnerLimit clear() {
        limits.clear();
        return this;
    }

    @Override
    public boolean test(TestCaseResult o) {
        return limits.stream().allMatch(l -> l.test(o));
    }

    @Override
    public String toString() {
        return limits.stream().map(Predicate::toString).collect(Collectors.joining(" && ", "[", "]"));
    }
}
