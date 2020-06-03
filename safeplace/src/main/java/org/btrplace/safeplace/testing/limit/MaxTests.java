/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing.limit;

import org.btrplace.safeplace.testing.Result;

import java.util.function.Predicate;

/**
 * A predicate to limit the number of tests.
 * @author Fabien Hermenier
 */
public class MaxTests implements Predicate<Result> {

    private int max;

    /**
     * New threshold.
     *
     * @param max the maximum number of tests
     */
    public MaxTests(int max) {
        this.max = max;
    }

    @Override
    public boolean test(Result tc) {
        max--;
        return max > 0;
    }

    @Override
    public String toString() {
        return "tests < " + max;
    }

}
