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

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.CumulatedRunningCapacity;
import btrplace.plan.event.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.CumulatedRunningCapacity} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.CumulatedRunningCapacity
 */
public class CumulatedRunningCapacityChecker extends AllowAllConstraintChecker<CumulatedRunningCapacity> {

    private int usage;

    private Set<UUID> srcRunnings;

    private int qty;

    /**
     * Make a new checker.
     *
     * @param c the associated constraint
     */
    public CumulatedRunningCapacityChecker(CumulatedRunningCapacity c) {
        super(c);
        qty = c.getAmount();
    }

    private boolean leave(UUID n) {
        if (getConstraint().isContinuous() && getNodes().contains(n)) {
            usage--;
        }
        return true;
    }

    private boolean arrive(UUID n) {
        return !(getConstraint().isContinuous() && getNodes().contains(n) && usage++ == qty);
    }

    @Override
    public boolean start(BootVM a) {
        return arrive(a.getDestinationNode());
    }

    @Override
    public boolean start(KillVM a) {
        if (getConstraint().isContinuous() && srcRunnings.remove(a.getVM())) {
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
            for (UUID n : getNodes()) {
                nb += map.getRunningVMs(n).size();
                if (nb > qty) {
                    return false;
                }
            }
            srcRunnings = new HashSet<>(map.getRunningVMs());
            track(srcRunnings);
        }
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        int nb = 0;
        Mapping map = mo.getMapping();
        for (UUID n : getNodes()) {
            nb += map.getRunningVMs(n).size();
            if (nb > qty) {
                return false;
            }
        }
        return true;
    }
}
