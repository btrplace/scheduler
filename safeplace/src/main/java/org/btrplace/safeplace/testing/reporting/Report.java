/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing.reporting;

import org.btrplace.safeplace.testing.DefaultTestCampaign;
import org.btrplace.safeplace.testing.TestCaseResult;

/**
 * Specify a report resulting from a {@link DefaultTestCampaign}.
 * @author Fabien Hermenier
 */
public interface Report {

    /**
     * Add a given result
     *
     * @param r the result to add
     */
    void with(TestCaseResult r);

    /**
     * Indicates the number of over-filtering test cases.
     *
     * @return a positive number
     */
    int overFiltering();

    /**
     * Indicates the number of under-filtering test cases.
     *
     * @return a positive number
     */
    int underFiltering();

    /**
     * Indicates the number of test cases that crashed
     *
     * @return a positive number
     */
    int failures();

    /**
     * Indicates the number of successful test cases.
     *
     * @return a positive number
     */
    int success();

    /**
     * Returns the number of defects that is the failures plus the over-filtering plus the under-filtering.
     *
     * @return a positive number
     */
    default int defects() {
        return failures() + overFiltering() + underFiltering();
    }

    /**
     * An event to signal the last test has been executed.
     */
    default void done() {
        //nothing to do by default
    }
}
