/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing;

import org.testng.Assert;

import java.util.function.Consumer;

/**
 * A utility class to predefine hooks when a defect is reported
 *
 * @author Fabien Hermenier
 */
public final class DefectHooks {

    /**
     * A hook that ignore the defect
     */
    public static final Consumer<TestCaseResult> ignore = res -> {
        //Ignore silently the defect
    };

    /**
     * A hook that print the defect and continue.
     */
    public static final Consumer<TestCaseResult> print = res -> {
        System.out.println(res);
    };

    /**
     * A hook that use an assertion to signal the defect
     */
    public static final Consumer<TestCaseResult> failedAssertion = res -> {
        assert false : res.toString();
    };

    /**
     * A hook that uses testng to signal a defect.
     */
    public static final Consumer<TestCaseResult> testNgFailure = res ->
            Assert.assertEquals(res.result(), Result.SUCCESS, res.toString());

    /**
     * Utility class. No instantiation.
     */
    private DefectHooks() {
    }
}
