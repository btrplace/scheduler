/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.ActionVisitor;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.AllocateEvent;
import org.btrplace.plan.event.BootNode;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.ForgeVM;
import org.btrplace.plan.event.KillVM;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ResumeVM;
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SubstitutedVMEvent;
import org.btrplace.plan.event.SuspendVM;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Detect dependencies between actions.
 * Actions are inserted using {@code #visit(...)} methods.
 *
 * @author Fabien Hermenier
 */
public class DependenciesExtractor implements ActionVisitor {

  private final Map<Action, Node> demandingNodes;

  private final Map<Node, Set<Action>> freeing;

  private final Map<Node, Set<Action>> demanding;

  private final Model origin;

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
        freeing.putIfAbsent(u, new HashSet<>());
        return freeing.get(u);
    }

    private Set<Action> getDemandings(Node u) {
        demanding.putIfAbsent(u, new HashSet<>());
        return demanding.get(u);
    }

    @Override
    public Boolean visit(Allocate a) {
        //If the resource allocation is increasing, it's
        //a consuming action. Otherwise, it's a freeing action
        String rcId = a.getResourceId();
        int newAmount = a.getAmount();
        ShareableResource rc = ShareableResource.get(origin, rcId);
        if (rc == null) {
            return false;
        }
        int oldAmount = rc.getConsumption(a.getVM());
        if (newAmount > oldAmount) {
            demandingNodes.put(a, a.getHost());
            return getDemandings(a.getHost()).add(a);
        }
        return getFreeings(a.getHost()).add(a);
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
        }
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
