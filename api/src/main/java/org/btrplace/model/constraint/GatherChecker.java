/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.event.RunningVMPlacement;

/**
 * Checker for the {@link org.btrplace.model.constraint.Gather} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Gather
 */
public class GatherChecker extends AllowAllConstraintChecker<Gather> {

    private Node usedInContinuous;

    /**
     * Make a new checker.
     *
     * @param g the associated constraint
     */
    public GatherChecker(Gather g) {
        super(g);
        usedInContinuous = null;
    }

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            Mapping map = mo.getMapping();
            for (VM vm : getVMs()) {
                if (map.isRunning(vm)) {
                    if (usedInContinuous == null) {
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
            if (usedInContinuous != null && a.getDestinationNode() != usedInContinuous) {
                return false;
            } else if (usedInContinuous == null) {
                usedInContinuous = a.getDestinationNode();
            }
        }
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Node used = null;
        Mapping map = mo.getMapping();
        for (VM vm : getVMs()) {
            if (map.isRunning(vm)) {
                if (used == null) {
                    used = map.getVMLocation(vm);
                } else if (used != map.getVMLocation(vm)) {
                    return false;
                }
            }
        }
        return true;
    }
}
