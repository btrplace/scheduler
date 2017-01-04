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

package org.btrplace.safeplace.testing.fuzzer;

import org.btrplace.safeplace.testing.TestCase;

import java.util.function.Supplier;

/**
 * Specify a {@link TestCase} fuzzer.
 *
 * @author Fabien Hermenier
 */
public interface Fuzzer extends Supplier<TestCase> {

    /**
     * The duration of the fuzzing stage.
     *
     * @return a duration in millisecond
     */
    long lastFuzzingDuration();

    /**
     * The duration of the validation stage.
     *
     * @return a duration in millisecond
     */
    long lastValidationDuration();

    /**
     * The number of fuzzing iterations to get a valid test case.
     *
     * @return an integer
     */
    int lastFuzzingIterations();
}
