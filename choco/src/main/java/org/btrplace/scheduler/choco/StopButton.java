/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco;

import org.chocosolver.util.criteria.Criterion;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A stop button that can stop the solving process.
 */
public class StopButton implements Criterion {

  /**
   * The stop flag.
   */
  private final AtomicBoolean stopNow = new AtomicBoolean(false);

  @Override
  public boolean isMet() {
    return stopNow.get();
  }

  /**
   * Stop the solver.
   */
  public void stopNow() {
    stopNow.set(true);
  }
}
