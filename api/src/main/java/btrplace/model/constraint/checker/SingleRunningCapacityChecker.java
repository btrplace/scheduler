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
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.SingleRunningCapacity;
import btrplace.plan.event.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Checker for the {@link btrplace.model.constraint.SingleRunningCapacity} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.SingleRunningCapacity
 */
public class SingleRunningCapacityChecker extends AllowAllConstraintChecker<SingleRunningCapacity> {

    private Map<Node, Integer> usage;

    private int amount;

    private Set<VM> srcRunnings;

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SingleRunningCapacityChecker(SingleRunningCapacity s) {
        super(s);
        amount = s.getAmount();
    }

    private boolean leave(Node n) {
        if (getConstraint().isContinuous() && getNodes().contains(n)) {
            usage.put(n, usage.get(n) - 1);
        }
        return true;
    }

    private boolean arrive(Node n) {
        if (getConstraint().isContinuous() && getNodes().contains(n)) {
            int u = usage.get(n);
            if (u == amount) {
                return false;
            }
            usage.put(n, u + 1);
        }
        return true;
    }

    @Override
    public boolean start(BootNode a) {
        if (getNodes().contains(a.getNode())) {
            usage.put(a.getNode(), 0);
        }
        return true;
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
        return leave(a.getSourceNode()) && arrive(a.getDestinationNode());
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
            Mapping map = mo.getMapping();
            usage = new HashMap<>(getNodes().size());
            for (Node n : getNodes()) {
                int s = map.getRunningVMs(n).size();
                if (s > amount) {
                    return false;
                }
                usage.put(n, s);
            }
            srcRunnings = new HashSet<>();
            for (Node n : map.getOnlineNodes()) {
                srcRunnings.addAll(map.getRunningVMs(n));
            }
            track(srcRunnings);
        }
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping map = mo.getMapping();
        for (Node n : getNodes()) {
            if (map.getRunningVMs(n).size() > amount) {
                return false;
            }
        }
        return true;
    }
}
