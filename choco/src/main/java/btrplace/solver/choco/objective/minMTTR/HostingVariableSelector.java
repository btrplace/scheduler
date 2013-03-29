/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace.solver.choco.objective.minMTTR;

import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceUtils;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;

/**
 * A variable selector that focuses the placement variables of slices.
 * Choco will try to instantiate the variables following the slice ordering
 *
 * @author Fabien Hermenier
 */
public class HostingVariableSelector extends AbstractIntVarSelector {

    private ReconfigurationProblem rp;

    private String label;

    private OnStableNodeFirst schedHeuristic;

    /**
     * Make a new heuristic.
     * By default, the heuristic doesn't touch the scheduling constraints.
     *
     * @param dbgLbl the debug label
     * @param rp     the rp to rely on
     * @param slices the slices to consider
     */
    public HostingVariableSelector(String dbgLbl, ReconfigurationProblem rp, List<Slice> slices, OnStableNodeFirst sched) {
        super(rp.getSolver(), SliceUtils.extractHosters(slices));
        this.schedHeuristic = sched;
        this.rp = rp;
        label = dbgLbl;
    }

    @Override
    public IntDomainVar selectVar() {
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].isInstantiated()) {
                //rp.getLogger().debug("{}: focus on VM {}", label, vars[i]);
                if (schedHeuristic != null) {
                    schedHeuristic.invalidPlacement();
                }
                return vars[i];
            }
        }
        rp.getLogger().debug("{}: no more VMs to handle", label);
        return null;
    }

}
