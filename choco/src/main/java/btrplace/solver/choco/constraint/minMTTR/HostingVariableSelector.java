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

import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceUtils;
import solver.search.strategy.selectors.VariableSelector;
import solver.variables.IntVar;

import java.util.List;

/**
 * A variable selector that focuses the placement variables of slices.
 * Choco will try to instantiate the variables following the slice ordering
 *
 * @author Fabien Hermenier
 */
public class HostingVariableSelector implements VariableSelector<IntVar> {

    private ReconfigurationProblem rp;

    private String label;

    private OnStableNodeFirst schedHeuristic;

    private IntVar[] vars;

    /**
     * Make a new heuristic.
     * By default, the heuristic doesn't touch the scheduling constraints.
     *
     * @param dbgLbl the debug label
     * @param p      the rp to rely on
     * @param slices the slices to consider
     */
    public HostingVariableSelector(String dbgLbl, ReconfigurationProblem p, List<Slice> slices, OnStableNodeFirst sched) {
        vars = SliceUtils.extractHosters(slices);
        this.schedHeuristic = sched;
        this.rp = p;
        label = dbgLbl;
    }

    @Override
    public IntVar[] getScope() {
        return vars;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public void advance() {

    }

    @Override
    public IntVar getVariable() {
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].instantiated()) {
                if (schedHeuristic != null) {
                    schedHeuristic.invalidPlacement();
                }
                System.out.println("Return " + vars[i]);
                return vars[i];
            }
        }
        System.out.println(label + " : no more VMs to handle");
        return null;
    }

}
