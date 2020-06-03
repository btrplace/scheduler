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
import org.btrplace.plan.event.BootNode;
import org.btrplace.plan.event.RunningVMPlacement;

import java.util.HashSet;
import java.util.Set;

/**
 * Checker for the {@link org.btrplace.model.constraint.Lonely} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Lonely
 */
public class LonelyChecker extends AllowAllConstraintChecker<Lonely> {

  private final Set<Node> idleNodes;

  private final Set<Node> privateNodes;

    /**
     * Make a new checker.
     *
     * @param l the associated constraint
     */
    public LonelyChecker(Lonely l) {
        super(l);
        idleNodes = new HashSet<>();
        privateNodes = new HashSet<>();
    }

    private boolean checkDestination(VM vm, Node n) {
        if (getConstraint().isContinuous()) {
            if (getVMs().contains(vm)) {
                if (!idleNodes.remove(n)) {
                    //The node was not idle
                    //So it must be private
                    return privateNodes.add(n);
                }
                //The node is no longer idle, just private
                return privateNodes.add(n);
            }
            if (!idleNodes.remove(n)) {
                return !privateNodes.contains(n);
            }
        }
        return true;
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        return checkDestination(a.getVM(), a.getDestinationNode());
    }

    private boolean discreteCheck(Model mo) {
        Mapping map = mo.getMapping();
        for (VM vm : getVMs()) {
            if (map.isRunning(vm)) {
                Node host = map.getVMLocation(vm);
                Set<VM> on = map.getRunningVMs(host);
                //Check for other VMs on the node. If they are not in the constraint
                //it's a violation
                for (VM vm2 : on) {
                    if (!vm2.equals(vm) && !getVMs().contains(vm2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean start(BootNode a) {
        return idleNodes.add(a.getNode());
    }

    @Override
    public boolean endsWith(Model mo) {
        return discreteCheck(mo);
    }

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            boolean ret = discreteCheck(mo);
            if (ret) {
                Mapping map = mo.getMapping();
                for (VM vm : getVMs()) {
                    if (map.isRunning(vm)) {
                        privateNodes.add(map.getVMLocation(vm));
                    }
                }
                for (Node n : map.getOnlineNodes()) {
                    if (map.getRunningVMs(n).isEmpty()) {
                        idleNodes.add(n);
                    }
                }
            }
            return ret;
        }
        return true;
    }
}
