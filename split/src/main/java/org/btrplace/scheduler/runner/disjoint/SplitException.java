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

package org.btrplace.scheduler.runner.disjoint;

import org.btrplace.model.Model;
import org.btrplace.scheduler.SchedulerModelingException;

/**
 * Signals an error when trying to split an instance.
 *
 * @author Fabien Hermenier
 */
public class SplitException extends SchedulerModelingException {

  /**
   * Make a new exception.
   *
   * @param m   the model.
   * @param msg the error message.
   */
  public SplitException(Model m, String msg) {
    super(m, msg);
  }

  /**
   * New exception.
   *
   * @param m   the problem to solve.
   * @param msg the error message.
   * @param t   the exception to rethrow.
   */
  public SplitException(Model m, String msg, Throwable t) {
    super(m, msg, t);
  }
}
