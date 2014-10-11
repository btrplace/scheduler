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
 * Checker for the {@link org.btrplace.model.constraint.Split} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Split
 */
public class SplitChecker extends AllowAllConstraintChecker<Split> {

    /**
     * The group of VMs.
     */
    private List<Set<VM>> vGroups;

    private Model mockModel;

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SplitChecker(Split s) {
        super(s);
        vGroups = new ArrayList<>(s.getSets().size());
        for (Collection<VM> set : s.getSets()) {
            Set<VM> ss = new HashSet<>(set);
            vGroups.add(ss);
            track(ss);
        }
    }

    @Override
    public boolean endsWith(Model mo) {
        //Catch the booked nodes for each set
        mockModel = new DefaultModel();
        MappingUtils.fill(mo.getMapping(), mockModel.getMapping());
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
        for (Collection<VM> vGroup : vGroups) {
            for (VM vmId : vGroup) {
                if (mockModel.getMapping().isRunning(vmId)) {
                    //Get the hosting server
                    //Check if only hosts VMs in its group
                    Node nId = mockModel.getMapping().getVMLocation(vmId);
                    for (VM vm : mockModel.getMapping().getRunningVMs(nId)) {
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
