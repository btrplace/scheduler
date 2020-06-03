/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler;

import org.btrplace.plan.ReconfigurationPlan;

/**
 * An exception to state the computed reconfiguration plan is not viable.
 * It basically indicates a bug inside the scheduler.
 *
 * @author Fabien Hermenier
 */
public class InconsistentSolutionException extends SchedulerModelingException {

  private final ReconfigurationPlan plan;

  /**
   * New exception.
   *
   * @param p   the faulty plan.
   * @param msg the error message.
   */
  public InconsistentSolutionException(ReconfigurationPlan p, String msg) {
    super(p.getOrigin(), msg);
    plan = p;
  }

  /**
   * Return the faulty plan.
   *
   * @return a reconfiguration plan.
   */
  public ReconfigurationPlan getResult() {
    return plan;
  }
}
