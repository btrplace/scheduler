/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco;

import org.btrplace.model.Instance;
import org.btrplace.model.VM;

import java.util.Set;


/**
 * An interface to specify an object that can estimated a supposed
 * set of misplaced VMs in a model.
 * <p>
 * This information will be used by the {@link ChocoScheduler}
 * to restrict the amount of VMs to consider in the scheduler
 * to a minimum.
 * <p>
 * The set of mis-placed VMs is not necessarily optimal but it must
 * be good enough to be able to compute a solution by only managing
 * these VMs.
 *
 * @author Fabien Hermenier
 */
@FunctionalInterface
public interface MisplacedVMsEstimator {


    /**
     * Get the VMs that are supposed to be mis-placed.
     *
     * @param i the instance to inspect
     * @return a set of VMs identifier that may be empty (when no VMs are misplaced)
     */
    Set<VM> getMisPlacedVMs(Instance i);
}
