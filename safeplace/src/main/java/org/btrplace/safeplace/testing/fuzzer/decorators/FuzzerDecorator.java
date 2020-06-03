/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing.fuzzer.decorators;

import org.btrplace.plan.ReconfigurationPlan;

/**
 * Specify a decorator that can alter a generated reconfiguration
 * @author Fabien Hermenier
 */
@FunctionalInterface
public interface FuzzerDecorator {

    /**
     * Decorate a given reconfiguration plan
     *
     * @param p the plan to decorate
     */
    void decorate(ReconfigurationPlan p);


}
