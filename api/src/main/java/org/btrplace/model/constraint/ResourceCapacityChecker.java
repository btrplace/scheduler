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
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.event.*;

/**
 * Checker for the {@link org.btrplace.model.constraint.ResourceCapacity} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.ResourceCapacity
 */
public class ResourceCapacityChecker extends AllowAllConstraintChecker<ResourceCapacity> {

    private ShareableResource rc;

    private int free;

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public ResourceCapacityChecker(ResourceCapacity s) {
        super(s);
    }

    private boolean leave(int amount, Node n) {
        if (getConstraint().isContinuous() && getNodes().contains(n)) {
            free += amount;
        }
        return true;
    }

    private boolean arrive(int amount, Node n) {
        if (getConstraint().isContinuous() && getNodes().contains(n)) {
            free -= amount;
            if (free < 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean start(BootVM a) {
        return arrive(rc.getConsumption(a.getVM()), a.getDestinationNode());
    }

    @Override
    public boolean start(KillVM a) {
        if (getConstraint().isContinuous()/* && srcRunning.remove(a.getVM())*/) {
            return leave(rc.getConsumption(a.getVM()), a.getNode());
        }
        return true;
    }

    @Override
    public boolean start(MigrateVM a) {
        if (getConstraint().isContinuous()) {
            if (!(getNodes().contains(a.getSourceNode()) && getNodes().contains(a.getDestinationNode()))) {
                return leave(rc.getConsumption(a.getVM()), a.getSourceNode()) && arrive(rc.getConsumption(a.getVM()), a.getDestinationNode());
            }
        }
        return true;
    }

    @Override
    public boolean start(ResumeVM a) {
        return arrive(rc.getConsumption(a.getVM()), a.getDestinationNode());
    }

    @Override
    public boolean start(ShutdownVM a) {
        return leave(rc.getConsumption(a.getVM()), a.getNode());
    }

    @Override
    public boolean start(SuspendVM a) {
        return leave(rc.getConsumption(a.getVM()), a.getSourceNode());
    }

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            rc = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE + getConstraint().getResource());
            free = getConstraint().getAmount();
            Mapping map = mo.getMapping();
            for (Node n : getNodes()) {
                free -= rc.sumConsumptions(map.getRunningVMs(n), true);
                if (free < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean start(Allocate e) {
        return arrive(rc.getConsumption(e.getVM()), e.getHost());
    }

    @Override
    public boolean consume(AllocateEvent e) {
        //TODO: Get its current location to check if it is on a node in the constraint
        return true;
    }

    @Override
    public boolean endsWith(Model i) {
        ShareableResource r = (ShareableResource) i.getView(ShareableResource.VIEW_ID_BASE + getConstraint().getResource());
        if (r == null) {
            return false;
        }

        int remainder = getConstraint().getAmount();
        for (Node id : getNodes()) {
            if (i.getMapping().isOnline(id)) {
                remainder -= r.sumConsumptions(i.getMapping().getRunningVMs(id), true);
                if (remainder < 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
