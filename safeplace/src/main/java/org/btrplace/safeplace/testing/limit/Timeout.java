/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing.limit;

import org.btrplace.safeplace.testing.Result;

import java.util.function.Predicate;

/**
 * A predicate to cap the testing duration.
 * @author Fabien Hermenier
 */
public class Timeout implements Predicate<Result> {

  private final int max;
  private long start;

    /**
     * New threshold.
     *
     * @param max the testing duration in seconds
     */
    public Timeout(int max) {
        this.max = max;
        start = -1;
    }

    @Override
    public boolean test(Result tc) {
        if (start == -1) {
            start = System.currentTimeMillis();
        }

        return System.currentTimeMillis() - start < max * 1000;
    }

    @Override
    public String toString() {
        return "duration <= " + max + " sec.";
    }

}
