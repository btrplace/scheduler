/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Mapping;
import org.btrplace.model.MappingUtils;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.event.RunningVMPlacement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checker for the {@link org.btrplace.model.constraint.SplitAmong} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.SplitAmong
 */
public class SplitAmongChecker extends AllowAllConstraintChecker<SplitAmong> {

  private final List<Set<VM>> vGroups;

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
                        if (chosenGroup.isEmpty() || !pUsed.add(chosenGroup)) {
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
