/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.AllocateEvent;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.KillVM;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ResumeVM;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SuspendVM;

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
          return free >= 0;
        }
        return true;
    }

    @Override
    public boolean start(BootVM a) {
        return arrive(rc.getConsumption(a.getVM()), a.getDestinationNode());
    }

    @Override
    public boolean start(KillVM a) {
        if (getConstraint().isContinuous()) {
            return leave(rc.getConsumption(a.getVM()), a.getNode());
        }
        return true;
    }

    @Override
    public boolean start(MigrateVM a) {
        if (getConstraint().isContinuous() &&
                !getNodes().contains(a.getSourceNode()) &&
                getNodes().contains(a.getDestinationNode())) {
            return leave(rc.getConsumption(a.getVM()), a.getSourceNode()) &&
                    arrive(rc.getConsumption(a.getVM()), a.getDestinationNode());
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
            rc = ShareableResource.get(mo, getConstraint().getResource());
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
        ShareableResource r = ShareableResource.get(i, getConstraint().getResource());
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
