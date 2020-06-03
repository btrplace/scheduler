/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco;

import org.btrplace.model.Model;
import org.chocosolver.memory.IEnvironment;

/**
 * Allows to choose a memory environment that is the most appropriate with regards
 * to the model to solve
 *
 * @author Fabien Hermenier
 */
@FunctionalInterface
public interface EnvironmentFactory {

    /**
     * Build the memory environment.
     *
     * @param mo the model that will be solved
     * @return the memory environment
     */
    IEnvironment build(Model mo);
}
