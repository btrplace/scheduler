/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.transition;


import org.chocosolver.solver.variables.BoolVar;

/**
 * An interface to specify an VM transition on a VM that is running and that will keep running.
 *
 * @author Fabien Hermenier
 */
public interface KeepRunningVM extends VMTransition {


    /**
     * Indicates if the VMs is staying on its current hosting node.
     *
     * @return a variable instantiated to {@code 1} iff the VM is staying on its current node.
     * Instantiated to {@code 0} otherwise
     */
    BoolVar isStaying();
}
