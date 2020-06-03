/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
