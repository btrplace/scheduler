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

import java.util.Collection;

/**
 * Checker for the {@link Among} constraint
 *
 * @author Fabien Hermenier
 * @see Among
 */
public class AmongChecker extends AllowAllConstraintChecker<Among> {

    /**
     * Current group (for the continuous restriction). {@code null} if no group has been selected.
     */
    private Collection<Node> selectedGroup = null;

    /**
     * Make a new checker.
     *
     * @param a the associated constraint
     */
    public AmongChecker(Among a) {
        super(a);
    }

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            Mapping map = mo.getMapping();
            for (VM vm : getVMs()) {
                if (map.isRunning(vm)) {
                    Collection<Node> nodes = getConstraint().getAssociatedPGroup(map.getVMLocation(vm));
                    if (nodes.isEmpty()) {
                        return false;
                    } else if (selectedGroup == null) {
                        selectedGroup = nodes;
                    } else if (!selectedGroup.equals(nodes)) {
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
            if (selectedGroup == null) {
                selectedGroup = getConstraint().getAssociatedPGroup(a.getDestinationNode());
                //disallowed group
                return !selectedGroup.isEmpty();
            } else {
                //Not the right group
                return selectedGroup.contains(a.getDestinationNode());
            }
        }
        return true;
    }

    @Override
    public boolean endsWith(Model i) {
        Mapping map = i.getMapping();
        Collection<Node> grp = null;
        for (VM vm : getVMs()) {
            if (map.isRunning(vm)) {
                Collection<Node> nodes = getConstraint().getAssociatedPGroup(map.getVMLocation(vm));
                if (nodes.isEmpty()) {
                    return false;
                } else if (grp == null) {
                    grp = nodes;
                } else if (!grp.equals(nodes)) {
                    return false;
                }
            }
        }
        return true;
    }
}
