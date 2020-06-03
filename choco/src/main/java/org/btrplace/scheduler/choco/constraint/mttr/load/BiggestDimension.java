/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint.mttr.load;

/**
 * Estimator stating the load of a node is the load of the mostly-loaded
 * dimension.
 * @author Fabien Hermenier
 */
public class BiggestDimension implements GlobalLoadEstimator {

    @Override
    public double getLoad(double[] loads) {
        double m = loads[0];
        for (int i = loads.length - 1; i > 0; i--) {
            m = Math.max(loads[i], m);
        }
        return m;
    }
}
