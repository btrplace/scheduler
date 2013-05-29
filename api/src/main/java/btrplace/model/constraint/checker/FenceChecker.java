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

package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.constraint.Fence;
import btrplace.plan.event.RunningVMPlacement;

import java.util.Set;

/**
 * Checker for the {@link btrplace.model.constraint.Fence} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Fence
 */
public class FenceChecker extends AllowAllConstraintChecker<Fence> {

    /**
     * Make a new checker.
     *
     * @param f the associated constraint
     */
    public FenceChecker(Fence f) {
        super(f);
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement r) {
        if (getVMs().contains(r.getVM())) {
            return getNodes().contains(r.getDestinationNode());
        }
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        Set<VM> runnings = c.getRunningVMs();
        for (VM vm : getVMs()) {
            if (runnings.contains(vm) && !getNodes().contains(c.getVMLocation(vm))) {
                return false;
            }
        }
        return true;
    }
}
