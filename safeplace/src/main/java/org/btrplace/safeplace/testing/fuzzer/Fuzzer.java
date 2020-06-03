/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
