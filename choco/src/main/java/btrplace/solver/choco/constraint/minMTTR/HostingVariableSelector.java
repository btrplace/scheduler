/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.constraint.minMTTR;

import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceUtils;
import solver.search.strategy.selectors.variables.InputOrder;
import solver.variables.IntVar;

import java.util.List;

/**
 * A variable selector that focuses the placement variables of slices.
 * Choco will try to instantiate the variables following the slice ordering
 *
 * @author Fabien Hermenier
 */
public class HostingVariableSelector extends InputOrder<IntVar> {

    private OnStableNodeFirst schedHeuristic;

    /**
     * Make a new heuristic.
     * By default, the heuristic doesn't touch the scheduling constraints.
     *
     * @param slices the slices to consider
     * @param sched  the scheduling heuristic to notify when the placement is invalidated
     */
    public HostingVariableSelector(List<Slice> slices, OnStableNodeFirst sched) {
        super(SliceUtils.extractHosters(slices));
        this.schedHeuristic = sched;
    }

    @Override
    public IntVar getVariable() {
        IntVar v = super.getVariable();
        if (schedHeuristic != null) {
            schedHeuristic.invalidPlacement();
        }
        return v;
    }

}
