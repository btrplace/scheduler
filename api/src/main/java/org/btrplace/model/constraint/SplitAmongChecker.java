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

import org.btrplace.model.*;
import org.btrplace.plan.event.RunningVMPlacement;

import java.util.*;

/**
 * Checker for the {@link org.btrplace.model.constraint.SplitAmong} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.SplitAmong
 */
public class SplitAmongChecker extends AllowAllConstraintChecker<SplitAmong> {

    private List<Set<VM>> vGroups;

    private Model mockModel;

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SplitAmongChecker(SplitAmong s) {
        super(s);
        vGroups = new ArrayList<>();
        for (Collection<VM> vGroup : s.getGroupsOfVMs()) {
            Set<VM> x = new HashSet<>(vGroup);
            track(x);
            vGroups.add(x);
        }
    }

    private boolean checkMapping(Mapping m) {
        Set<Collection<Node>> pUsed = new HashSet<>();
        for (Set<VM> vGroup : vGroups) {
            Collection<Node> chosenGroup = null;
            //Check every running VM in a single vGroup are running in the same pGroup
            for (VM vmId : vGroup) {
                if (m.isRunning(vmId)) {
                    if (chosenGroup == null) {
                        chosenGroup = getConstraint().getAssociatedPGroup(m.getVMLocation(vmId));
                        if (chosenGroup == null || !pUsed.add(chosenGroup)) {
                            //== null : The VM is running but on an unknown group. It is an error
                            // !add: The pGroup has already been used for another set of VMs.
                            return false;
                        }
                    } else if (!chosenGroup.contains(m.getVMLocation(vmId))) {
                        //The VM is not in the group with the other
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            mockModel = new DefaultModel();
            MappingUtils.fill(mo.getMapping(), mockModel.getMapping());
            return endsWith(mockModel);
        }
        return true;
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        if (getConstraint().isContinuous()) {
            a.apply(mockModel);
            return endsWith(mockModel);
        }
        return true;
    }

    @Override
    public boolean endsWith(Model i) {
        return checkMapping(i.getMapping());
    }
}
