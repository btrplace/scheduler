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

package org.btrplace.fuzzer;

import org.btrplace.model.Instance;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;

import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class Result {

    private Instance i;

    private Exception ex;

    private ReconfigurationPlan p;

    public Result(Instance i, ReconfigurationPlan p, Exception e) {
        this.i = i;
        this.ex = e;
        this.p = p;
    }

    public Instance instance() {
        return i;
    }

    public Exception exception() {
        return ex;
    }

    public ReconfigurationPlan plan() {
        return p;
    }

    public boolean succeed() {
        return ex == null && (p == null || p.isApplyable());
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(i.getModel());
        b.append("Constraints:\n\t");
        b.append(i.getSatConstraints().stream().map(SatConstraint::toString).collect(Collectors.joining("\n\t")));
        b.append("\n\n");
        if (p == null) {
            b.append("plan: -");
        } else {
            b.append("plan:\n").append(p);
        }
        b.append("\n\nException: ").append(ex == null ? "-" : ex.getMessage()).append("\n");
        if (p != null) {
            b.append("Applyable: ").append(p.isApplyable() ? "OK" : "NO").append("\n");
        }

        return b.toString();
    }
}
