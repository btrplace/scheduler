/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;

/**
 * Monitor to maintain start + duration = end
 *
 * @author Fabien Hermenier
 */
public class TaskMonitor implements IVariableMonitor<IntVar> {

  private final IntVar start;
  private final IntVar duration;
  private final IntVar end;

  /**
   * Make a new monitor.
   *
   * @param start    the task start moment
   * @param duration the task duration
   * @param end      the task end
   */
  private TaskMonitor(IntVar start, IntVar duration, IntVar end) {
    this.start = start;
        this.duration = duration;
        this.end = end;

        this.start.addMonitor(this);
        this.duration.addMonitor(this);
        this.end.addMonitor(this);
    }

    /**
     * Make a new Monitor
     *
     * @param start    the task start moment
     * @param duration the task duration
     * @param end      the task end
     * @return the resulting task.
     */
    public static TaskMonitor build(IntVar start, IntVar duration, IntVar end) {
        return new TaskMonitor(start, duration, end);
    }

    @Override
    public void onUpdate(IntVar vv, IEventType evt) throws ContradictionException {
        // start
        start.updateBounds(end.getLB() - duration.getUB(), end.getUB() - duration.getLB(), this);
        // end
        end.updateBounds(start.getLB() + duration.getLB(), start.getUB() + duration.getUB(), this);
        // duration
        duration.updateBounds(end.getLB() - start.getUB(), end.getUB() - start.getLB(), this);
    }
}
