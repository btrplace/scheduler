/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler;

import org.btrplace.model.Model;

/**
 * An exception to state the solver did not have enough time to state
 * about the problem feasibility.
 *
 * @author Fabien Hermenier
 */
public class UnstatableProblemException extends SchedulerException {

  /**
   * The allotted duration in seconds.
   */
  private final int duration;

  /**
   * Make a new exception.
   *
   * @param m  th model to solve.
   * @param to the allotted time in seconds.
   */
  public UnstatableProblemException(Model m, int to) {
    super(m, String.format("Unable to state about the problem feasibility within the allotted %s seconds", to));
    this.duration = to;
  }

  /**
   * Get the allotted time.
   *
   * @return a duration in second.
   */
  public int timeout() {
    return duration;
  }
}
