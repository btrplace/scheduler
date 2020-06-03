/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
