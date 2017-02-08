/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
  private int duration;

  /**
   * Make a new exception.
   *
   * @param m  th model to solve.
   * @param to the allotted time in seconds.
   */
  public UnstatableProblemException(Model m, int to) {
    super(m, String.format("Unable to state about the problem feasibility withing the allotted %s seconds", to));
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
