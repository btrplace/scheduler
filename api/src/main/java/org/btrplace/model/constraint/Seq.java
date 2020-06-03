/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.VM;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A constraint to force the actions that change the given VMs state
 * to be executed in the given order.
 * <p>
 * The restriction provided by the constraint is only continuous.
 *
 * @author Fabien Hermenier
 */

@SideConstraint(args = {"sched : lists(vms)"}, inv = "!(i, j : range(sched)) i < j --> (!(ai : actions(sched[i])) !(aj : actions(sched[j])) end(aj) <= begin(aj))")
public class Seq implements SatConstraint {

  private final List<VM> order;

    /**
     * Make a new constraint.
     *
     * @param seq the order to ensure
     */
    public Seq(List<VM> seq) {
        Set<VM> s = new HashSet<>(seq);
        if (s.size() != seq.size()) {
            throw new IllegalArgumentException("The list of VMs must not contain duplicates");
        }
        order = seq;
    }

    @Override
    public List<VM> getInvolvedVMs() {
        return order;
    }


    @Override
    public String toString() {
        return "seq(vms=" + order + ", continuous" + ')';
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    @Override
    public SatConstraintChecker<Seq> getChecker() {
        return new SeqChecker(this);
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Seq seq = (Seq) o;
        return Objects.equals(order, seq.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order);
    }
}
