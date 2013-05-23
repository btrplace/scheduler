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
import btrplace.model.constraint.SplitAmong;
import btrplace.plan.event.RunningVMPlacement;

import java.util.*;

/**
 * Checker for the {@link btrplace.model.constraint.SplitAmong} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.SplitAmong
 */
public class SplitAmongChecker extends AllowAllConstraintChecker<SplitAmong> {

    private List<Set<UUID>> vGrps;

    private Model mockModel;

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SplitAmongChecker(SplitAmong s) {
        super(s);
        vGrps = new ArrayList<>();
        for (Set<UUID> vGroup : s.getGroupsOfVMs()) {
            Set<UUID> x = new HashSet<>(vGroup);
            track(x);
            vGrps.add(x);
        }
    }

    private boolean checkMapping(Mapping m) {
        Set<Set<UUID>> pUsed = new HashSet<>();
        for (Set<UUID> vgrp : vGrps) {
            Set<UUID> choosedGroup = null;
            //Check every running VM in a single vgroup are running in the same pgroup
            for (UUID vmId : vgrp) {
                if (m.getRunningVMs().contains(vmId)) {
                    if (choosedGroup == null) {
                        choosedGroup = getConstraint().getAssociatedPGroup(m.getVMLocation(vmId));
                        if (choosedGroup == null) {
                            //Te VM is running but on an unknown group. It is an error
                            return false;
                        } else if (!pUsed.add(choosedGroup)) {
                            //The pgroup has already been used for another set of VMs.
                            return false;
                        }
                    } else if (!choosedGroup.contains(m.getVMLocation(vmId))) {
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
            Mapping curMapping = mo.getMapping().clone();
            mockModel = new DefaultModel(curMapping);
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
