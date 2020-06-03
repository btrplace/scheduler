/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing.verification.btrplace;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SatConstraintChecker;
import org.btrplace.model.constraint.SimpleConstraint;

import java.util.Objects;

/**
 * @author Fabien Hermenier
 */
public class Schedule extends SimpleConstraint {

  private final int start;
  private final int end;
  private VM vm;

  private Node n;

  public Schedule(VM v, int st, int ed) {
    super(true);
    this.start = st;
    this.end = ed;
    vm = v;
  }

    public Schedule(Node n, int st, int ed) {
        super(true);
        this.start = st;
        this.end = ed;
        this.n = n;
    }


    public VM getVM() {
        return vm;
    }

    public Node getNode() {
        return n;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "schedule(" + (vm == null ? n : vm) + ", " + start + "," + end + ")";
    }

    @Override
    public SatConstraintChecker<?> getChecker() {
        return new ScheduleChecker(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        Schedule schedule = (Schedule) o;
        return start == schedule.start &&
                end == schedule.end &&
                Objects.equals(vm, schedule.vm) &&
                Objects.equals(n, schedule.n);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, vm, n);
    }
}
