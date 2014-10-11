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

package org.btrplace.plan;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.event.*;

import java.util.*;

/**
 * Detect dependencies between actions.
 * Actions are inserted using {@code #visit(...)} methods.
 *
 * @author Fabien Hermenier
 */
public class DependenciesExtractor implements ActionVisitor {

    private Map<Action, Node> demandingNodes;

    private Map<Node, Set<Action>> freeing;

    private Map<Node, Set<Action>> demanding;

    private Model origin;

    /**
     * Make a new instance.
     *
     * @param o the model at the source of the reconfiguration plan
     */
    public DependenciesExtractor(Model o) {
        demanding = new HashMap<>();
        freeing = new HashMap<>();
        this.demandingNodes = new HashMap<>();
        origin = o;
    }

    private Set<Action> getFreeings(Node u) {
        Set<Action> actions = freeing.get(u);
        if (actions == null) {
            actions = new HashSet<>();
            freeing.put(u, actions);
        }
        return actions;
    }

    private Set<Action> getDemandings(Node u) {
        Set<Action> actions = demanding.get(u);
        if (actions == null) {
            actions = new HashSet<>();
            demanding.put(u, actions);
        }
        return actions;
    }

    @Override
    public Boolean visit(Allocate a) {
        //If the resource allocation is increasing, it's
        //a consuming action. Otherwise, it's a freeing action
        String rcId = a.getResourceId();
        int newAmount = a.getAmount();
        ShareableResource rc = (ShareableResource) origin.getView(ShareableResource.VIEW_ID_BASE + rcId);
        if (rc == null) {
            return false;
        }
        int oldAmount = rc.getConsumption(a.getVM());
        if (newAmount > oldAmount) {
            demandingNodes.put(a, a.getHost());
            return getDemandings(a.getHost()).add(a);
        } else {
            return getFreeings(a.getHost()).add(a);
        }
    }

    @Override
    public Boolean visit(AllocateEvent a) {
        return true;
    }

    @Override
    public Boolean visit(BootNode a) {
        return getFreeings(a.getNode()).add(a);
    }

    @Override
    public Boolean visit(BootVM a) {
        boolean ret = getDemandings(a.getDestinationNode()).add(a);
        demandingNodes.put(a, a.getDestinationNode());
        return ret;
    }

    @Override
    public Boolean visit(ForgeVM a) {
        /*TODO: true for the moment, but if we allow to chain
         forge with boot, it will no longer be as there will
        be a dependency on the VM (and not the node)*/
        return true;
    }

    @Override
    public Boolean visit(KillVM a) {
        return getFreeings(a.getNode()).add(a);
    }

    @Override
    public Boolean visit(MigrateVM a) {
        boolean ret = getFreeings(a.getSourceNode()).add(a) && getDemandings(a.getDestinationNode()).add(a);
        demandingNodes.put(a, a.getDestinationNode());
        return ret;
    }

    @Override
    public Boolean visit(ResumeVM a) {
        boolean ret = getDemandings(a.getDestinationNode()).add(a);
        demandingNodes.put(a, a.getDestinationNode());
        return ret;
    }

    @Override
    public Boolean visit(ShutdownNode a) {
        boolean ret = getDemandings(a.getNode()).add(a);
        demandingNodes.put(a, a.getNode());
        return ret;
    }

    @Override
    public Boolean visit(ShutdownVM a) {
        return getFreeings(a.getNode()).add(a);
    }

    @Override
    public Boolean visit(SuspendVM a) {
        return getFreeings(a.getSourceNode()).add(a);
    }

    @Override
    public Object visit(SubstitutedVMEvent a) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the dependencies for an action.
     *
     * @param a the action to check
     * @return its dependencies, may be empty
     */
    public Set<Action> getDependencies(Action a) {
        if (!demandingNodes.containsKey(a)) {
            return Collections.emptySet();
        } else {
            Node n = demandingNodes.get(a);
            Set<Action> allActions = getFreeings(n);
            Set<Action> pre = new HashSet<>();
            for (Action action : allActions) {
                if (!action.equals(a) && a.getStart() >= action.getEnd()) {
                    pre.add(action);
                }
            }
            return pre;
        }
    }
}
