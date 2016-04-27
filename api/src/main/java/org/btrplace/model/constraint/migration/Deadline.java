/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.model.constraint.migration;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SatConstraint;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A constraint to force the maximum end time of a migration by an absolute
 * or relative deadline in the form of a timestamp.
 * 
 * @author Vincent Kherbache
 */
public class Deadline implements SatConstraint {

    private String timestamp;

    private VM vm;

    /**
     * Make a new constraint.
     *
     * @param vm        the VM to constraint
     * @param timestamp the desired deadline
     */
    public Deadline(VM vm, String timestamp) {
        this.vm = vm;
        this.timestamp = timestamp;
    }

    /**
     * Get the deadline timestamp.
     *
     * @return  the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Change the deadline timestamp.
     *
     * @param timestamp the new timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    @Override
    public Collection<Node> getInvolvedNodes() {
        return Collections.emptyList();
    }

    @Override
    public Collection<VM> getInvolvedVMs() {
        return Collections.singleton(vm);
    }

    @Override
    public boolean isContinuous() {
        return false;
    }

    @Override
    public DeadlineChecker getChecker() {
        return new DeadlineChecker(this);
    }

    @Override
    public String toString() {
        return "deadline(vm=" + vm + ", deadline='" + timestamp + "', continuous)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Deadline deadline = (Deadline) o;
        return Objects.equals(timestamp, deadline.timestamp) &&
                Objects.equals(vm, deadline.vm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, vm);
    }

    /**
     * Instantiate discrete constraints for a collection of VMs.
     *
     * @param vms      the VMs to integrate
     * @param deadline the desired deadline
     * @return the associated list of constraints
     */
    public static List<Deadline> newDeadline(Collection<VM> vms, String deadline) {
        return vms.stream().map(v -> new Deadline(v, deadline)).collect(Collectors.toList());
    }
}
