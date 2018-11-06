/*
 * Copyright (c) 2018 University Nice Sophia Antipolis
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
  private AtomicBoolean stopNow = new AtomicBoolean(false);

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
