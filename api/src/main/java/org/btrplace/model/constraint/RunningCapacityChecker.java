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

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.event.*;

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

    private int qty;

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
