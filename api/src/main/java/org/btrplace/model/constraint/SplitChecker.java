/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.DefaultModel;
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
 * Checker for the {@link org.btrplace.model.constraint.Split} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Split
 */
public class SplitChecker extends AllowAllConstraintChecker<Split> {

  /**
   * The group of VMs.
   */
  private final List<Set<VM>> vGroups;

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
