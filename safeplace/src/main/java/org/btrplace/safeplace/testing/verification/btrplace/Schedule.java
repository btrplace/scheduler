/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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

    private int start, end;

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
    public SatConstraintChecker getChecker() {
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
