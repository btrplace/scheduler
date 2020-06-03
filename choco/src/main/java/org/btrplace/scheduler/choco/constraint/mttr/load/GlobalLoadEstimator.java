/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint.mttr.load;

/**
 * Interface modeling a global load estimator.
 * It allows to consider multiple dimensions and reduce it to a unique metric.
 *
 * @author Fabien Hermenier
 */
@FunctionalInterface
public interface GlobalLoadEstimator {

    /**
     * Compute the load.
     *
     * @param loads the per dimension load (discretised)
     * @return the resulting load
     */
    double getLoad(double[] loads);
}
