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
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.KillVM;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ResumeVM;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SuspendVM;

import java.util.HashSet;
import java.util.Set;

/**
 * Checker for the {@link org.btrplace.model.constraint.RunningCapacity} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.RunningCapacity
 */
public class RunningCapacityChecker extends AllowAllConstraintChecker<RunningCapacity> {

    private int usage;

    private Set<VM> srcRunning;

  private final int qty;

    /**
     * Make a new checker.
     *
     * @param c the associated constraint
     */
    public RunningCapacityChecker(RunningCapacity c) {
        super(c);
        qty = c.getAmount();
    }

    private boolean leave(Node n) {
        if (getConstraint().isContinuous() && getNodes().contains(n)) {
            usage--;
        }
        return true;
    }

    private boolean arrive(Node n) {
        return !(getConstraint().isContinuous() && getNodes().contains(n) && usage++ == qty);
    }

    @Override
    public boolean start(BootVM a) {
        return arrive(a.getDestinationNode());
    }

    @Override
    public boolean start(KillVM a) {
        if (getConstraint().isContinuous() && srcRunning.remove(a.getVM())) {
            return leave(a.getNode());
        }
        return true;
    }

    @Override
    public boolean start(MigrateVM a) {
        if (getConstraint().isContinuous()) {
            if (!(getNodes().contains(a.getSourceNode()) && getNodes().contains(a.getDestinationNode()))) {
                return leave(a.getSourceNode()) && arrive(a.getDestinationNode());
            }
        }
        return true;
    }

    @Override
    public boolean start(ResumeVM a) {
        return arrive(a.getDestinationNode());
    }

    @Override
    public boolean start(ShutdownVM a) {
        return leave(a.getNode());
    }

    @Override
    public boolean start(SuspendVM a) {
        return leave(a.getSourceNode());
    }

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            int nb = 0;
            Mapping map = mo.getMapping();
            for (Node n : getNodes()) {
                nb += map.getRunningVMs(n).size();
                if (nb > qty) {
                    return false;
                }
            }
            srcRunning = new HashSet<>();
            for (Node n : map.getOnlineNodes()) {
                srcRunning.addAll(map.getRunningVMs(n));
            }
            track(srcRunning);
        }
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        int nb = 0;
        Mapping map = mo.getMapping();
        for (Node n : getNodes()) {
            nb += map.getRunningVMs(n).size();
            if (nb > qty) {
                return false;
            }
        }
        return true;
    }
}
