/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing;

/**
 * The possible outcomes for a test.
 * @author Fabien Hermenier
 */
public enum Result {

    /**
     * The implementation generates a plan that is not valid wrt. its spec. It let some inconsistencies pass.
     */
    UNDER_FILTERING,

    /**
     * The implementation did not generate a plan despite it exists. It denied solutions.
     */
    OVER_FILTERING,

    /**
     * The implementation crashed
     */
    CRASH,

    /**
     * The implementation and the specification are consistent.
     */
    SUCCESS
}
