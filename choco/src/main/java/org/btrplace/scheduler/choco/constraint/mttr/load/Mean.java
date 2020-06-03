/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint.mttr.load;

/**
 * Load estimator stating the global load is the average of the individual passed load.
 * @author Fabien Hermenier
 */
public class Mean implements GlobalLoadEstimator {

    @Override
    public double getLoad(double[] loads) {
        double t = 0;
        for (double d : loads) {
            t += d;
        }
        return t / loads.length;
    }
}
