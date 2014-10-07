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

package org.btrplace.model.constraint;

import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A constraint to force vms' actions to be executed
 * at the beginning (at time t=0), without any delay.
 * <p>
 * Created by vkherbac on 01/09/14.
 */
public class NoDelay extends SatConstraint {

    /**
     * Instantiate constraints for a collection of VMs.
     *
     * @param vms the VMs to integrate
     * @return the associated list of constraints
     */
    public static List<NoDelay> newNoDelay(Collection<VM> vms) {
        List<NoDelay> l = new ArrayList<>(vms.size());
        for (VM v : vms) {
            l.add(new NoDelay(v));
        }
        return l;
    }

    /**
     * Make a new constraint.
     *
     * @param vm the vm to restrict
     */
    public NoDelay(VM vm) {

        super(Collections.singleton(vm), Collections.<Node>emptyList(), true);
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new NoDelayChecker(this);
    }

    @Override
    public String toString() {
        return "noDelay(" + "vm=" + getInvolvedVMs() + ", " + restrictionToString() + ")";
    }
}
