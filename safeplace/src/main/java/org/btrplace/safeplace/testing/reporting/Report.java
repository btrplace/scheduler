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

package org.btrplace.safeplace.testing.reporting;

import org.btrplace.safeplace.testing.TestCaseResult;

/**
 * Specify a report resulting from a {@link org.btrplace.safeplace.testing.TestCampaign}.
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
    default void done() {}
}
