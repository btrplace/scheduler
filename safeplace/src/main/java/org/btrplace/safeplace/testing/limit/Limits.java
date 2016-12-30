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

import org.btrplace.safeplace.testing.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Define stop criteria for a test campaign.
 * Criteria can be accumulated and updated.
 * When a new limit is set, any existing limit having the same class is removed.
 *
 * @author Fabien Hermenier
 */
public class Limits implements Predicate<Result> {

    private List<Predicate<Result>> predicates;

    /**
     * New limit, with no registered predicates.
     */
    public Limits() {
        predicates = new ArrayList<>();
    }

    /**
     * Specify a timeout in second.
     *
     * @param nb the campaign duration
     * @return {@code this}
     */
    public Limits seconds(int nb) {
        with(new Timeout(nb));
        return this;
    }

    /**
     * Specify a maximum number of tests to run.
     *
     * @param nb the number of tests
     * @return {@code this}
     */
    public Limits tests(int nb) {
        with(new MaxTests(nb));
        return this;
    }

    /**
     * Specify a timeout in second.
     *
     * @param nb the campaign duration
     * @return {@code this}
     */
    public Limits failures(int nb) {
        with(new MaxDefects(nb));
        return this;
    }

    /**
     * Specify a maximum number of successful tests
     *
     * @param nb the upper bound
     * @return {@code this}
     */
    public Limits success(int nb) {
        with(new MaxSuccess(nb));
        return this;
    }

    /**
     * Add a new limit.
     * Every previously registered predicates of the same class are removed
     *
     * @param p the predicate modeling the limit
     * @return {@code this}
     */
    public Limits with(Predicate<Result> p) {
        predicates.removeIf(r -> r.getClass().equals(p.getClass()));
        predicates.add(p);
        return this;
    }

    /**
     * Clean all the registered predicates.
     * @return {@code this}
     */
    public Limits clear() {
        predicates.clear();
        return this;
    }

    @Override
    public boolean test(Result o) {
        return predicates.stream().allMatch(l -> l.test(o));
    }

    @Override
    public String toString() {
        return predicates.stream().map(Predicate::toString).collect(Collectors.joining(" && ", "[", "]"));
    }
}
