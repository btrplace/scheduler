/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.scheduler.choco.Injectable;
import org.btrplace.scheduler.choco.MisplacedVMsEstimator;

/**
 * An interface to describe a constraint implementation for the solver.
 *
 * @author Fabien Hermenier
 */
public interface ChocoConstraint extends MisplacedVMsEstimator, Injectable {

}
