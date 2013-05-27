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
import btrplace.model.constraint.Gather;
import btrplace.plan.event.RunningVMPlacement;

/**
 * Checker for the {@link btrplace.model.constraint.Gather} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Gather
 */
public class GatherChecker extends AllowAllConstraintChecker<Gather> {

    private int usedInContinuous;

    /**
     * Make a new checker.
     *
     * @param g the associated constraint
     */
    public GatherChecker(Gather g) {
        super(g);
        usedInContinuous = -1;
    }

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            Mapping map = mo.getMapping();
            for (int vm : getVMs()) {
                if (map.getRunningVMs().contains(vm)) {
                    if (usedInContinuous < 0) {
                        usedInContinuous = map.getVMLocation(vm);
                    } else if (usedInContinuous != map.getVMLocation(vm)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        if (getConstraint().isContinuous() && getVMs().contains(a.getVM())) {
            if (usedInContinuous >= 0 && a.getDestinationNode() != usedInContinuous) {
                return false;
            } else if (usedInContinuous < 0) {
                usedInContinuous = a.getDestinationNode();
            }
        }
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        int used = -1;
        Mapping map = mo.getMapping();
        for (int vm : getVMs()) {
            if (map.getRunningVMs().contains(vm)) {
                if (used == -1) {
                    used = map.getVMLocation(vm);
                } else if (used != map.getVMLocation(vm)) {
                    return false;
                }
            }
        }
        return true;
    }
}
