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
 * Signals an error while modeling the scheduler.
 *
 * @author Fabien Hermenier
 */
public class SchedulerModelingException extends SchedulerException {

  /**
   * New exception.
   *
   * @param m   the problem to solve.
   * @param msg the error message.
   */
  public SchedulerModelingException(Model m, String msg) {
    super(m, msg);
  }

  /**
   * New exception.
   *
   * @param m   the problem to solve.
   * @param msg the error message.
   * @param t   the exception to rethrow.
   */
  public SchedulerModelingException(Model m, String msg, Throwable t) {
    super(m, msg, t);
  }

  /**
   * Signals a view is missing.
   *
   * @param m      the associated model.
   * @param viewId the awaited view.
   * @return the resulting exception.
   */
  public static SchedulerModelingException missingView(Model m, String viewId) {
    return new SchedulerModelingException(m, "View '" + viewId + "' is required but missing");
  }
}
