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
import org.btrplace.plan.event.KillVM;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.RunningVMPlacement;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SuspendVM;

import java.util.HashSet;
import java.util.Set;

/**
 * Checker for the {@link org.btrplace.model.constraint.Spread} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Ban
 */
public class SpreadChecker extends AllowAllConstraintChecker<Spread> {

  private final Set<Node> denied;

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SpreadChecker(Spread s) {
        super(s);
        denied = new HashSet<>();
    }


    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            Mapping map = mo.getMapping();
            for (VM vm : getVMs()) {
                Node n = map.getVMLocation(vm);
                if (n != null) {
                    denied.add(n);
                }
            }
        }
        return true;
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        if (getConstraint().isContinuous() && getVMs().contains(a.getVM())) {
            if (denied.contains(a.getDestinationNode())) {
                return false;
            }
            denied.add(a.getDestinationNode());
        }
        return true;
    }

    @Override
    public void end(MigrateVM a) {
        unDenied(a.getVM(), a.getSourceNode());
    }

    private void unDenied(VM vm, Node n) {
        if (getConstraint().isContinuous() && getVMs().contains(vm)) {
            denied.remove(n);
        }
    }

    @Override
    public void end(ShutdownVM a) {
        unDenied(a.getVM(), a.getNode());
    }

    @Override
    public void end(SuspendVM a) {
        unDenied(a.getVM(), a.getSourceNode());
    }

    @Override
    public void end(KillVM a) {
        unDenied(a.getVM(), a.getNode());
    }

    @Override
    public boolean endsWith(Model mo) {
        Set<Node> forbidden = new HashSet<>();
        Mapping map = mo.getMapping();
        for (VM vm : getVMs()) {
            if (map.isRunning(vm) && !forbidden.add(map.getVMLocation(vm))) {
                return false;
            }
        }
        return true;
    }
}
