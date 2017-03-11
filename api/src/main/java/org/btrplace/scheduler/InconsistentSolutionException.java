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
import org.btrplace.plan.ReconfigurationPlan;

/**
 * An exception to state the computed reconfiguration plan is not viable.
 * It basically indicates a bug inside the scheduler.
 *
 * @author Fabien Hermenier
 */
public class InconsistentSolutionException extends SchedulerModelingException {

  private ReconfigurationPlan plan;

  /**
   * New exception.
   *
   * @param m   the problem to solve.
   * @param p   the faulty plan.
   * @param msg the error message.
   */
  public InconsistentSolutionException(Model m, ReconfigurationPlan p, String msg) {
    super(m, msg);
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
