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

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Split;
import btrplace.plan.event.RunningVMPlacement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Split} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Split
 */
public class SplitChecker extends AllowAllConstraintChecker<Split> {

    /**
     * The group of VMs.
     */
    private List<Set<UUID>> vGroups;

    private Mapping curMapping;

    private Model mockModel;

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SplitChecker(Split s) {
        super(s);
        vGroups = new ArrayList<>(s.getSets());
        for (Set<UUID> set : vGroups) {
            track(set);
        }
    }

    @Override
    public boolean endsWith(Model mo) {
        //Catch the booked nodes for each set
        curMapping = mo.getMapping().clone();
        mockModel = new DefaultModel(curMapping);
        return checkModel();
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        if (getConstraint().isContinuous() && getVMs().contains(a.getVM())) {
            a.apply(mockModel);
            return checkModel();
        }
        return true;
    }

    private boolean checkModel() {
        for (Set<UUID> vGroup : vGroups) {
            for (UUID vmId : vGroup) {
                if (curMapping.getRunningVMs().contains(vmId)) {
                    //Get the hosting server
                    //Check if only hosts VMs in its group
                    UUID nId = curMapping.getVMLocation(vmId);
                    for (UUID vm : curMapping.getRunningVMs(nId)) {
                        if (getVMs().contains(vm) && !vGroup.contains(vm)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            return endsWith(mo);
        }
        return true;
    }
}
