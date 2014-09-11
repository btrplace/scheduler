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

package btrplace.safeplace;

import btrplace.model.*;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanAppender implements ActionVisitor {

    private ReconfigurationPlan base;

    private Model src;

    private Map<Node, Node> nodeOffset;

    private Map<VM, VM> vmOffset;

    public ReconfigurationPlanAppender() {
        this(new DefaultReconfigurationPlan(new DefaultModel()));
        nodeOffset = new HashMap<>();
        vmOffset = new HashMap<>();

    }


    public ReconfigurationPlanAppender(ReconfigurationPlan p) {
        src = p.getOrigin().clone();
        base = new DefaultReconfigurationPlan(src);
    }

    public ReconfigurationPlan getResult() {
        return base;
    }

    public void append(ReconfigurationPlan p) {
        //for each node and VM we create an equivalent,
        Mapping map = p.getOrigin().getMapping();
        vmOffset.clear();
        nodeOffset.clear();
        for (Node oldNode : map.getOnlineNodes()) {
            Node newNode = src.newNode();
            nodeOffset.put(oldNode, newNode);
            src.getMapping().addOnlineNode(newNode);
            for (VM oldVM : map.getRunningVMs(oldNode)) {
                VM newVM = src.newVM();
                vmOffset.put(oldVM, newVM);
                src.getMapping().addRunningVM(newVM, newNode);
            }
            for (VM oldVM : map.getSleepingVMs(oldNode)) {
                VM newVM = src.newVM();
                vmOffset.put(oldVM, newVM);
                src.getMapping().addSleepingVM(newVM, newNode);
            }
        }
        for (VM oldVM : map.getReadyVMs()) {
            VM newVM = src.newVM();
            vmOffset.put(oldVM, newVM);
            src.getMapping().addReadyVM(newVM);
        }
        for (Node oldNode : map.getOfflineNodes()) {
            Node newNode = src.newNode();
            nodeOffset.put(oldNode, newNode);
            src.getMapping().addOfflineNode(newNode);
        }

        /*System.out.println("---");
        System.out.println(vmOffset);
        System.out.println(map);*/
        //Convert the actions
        for (Action a : p) {
            Action to = (Action) a.visit(this);
            //System.out.println(a + " -> " + to);
            base.add(to);
        }
    }

    @Override
    public Object visit(Allocate a) {
        return new Allocate(vmOffset.get(a.getVM()),
                nodeOffset.get(a.getHost()),
                a.getResourceId(),
                a.getAmount(),
                a.getStart(),
                a.getEnd());
    }

    @Override
    public Object visit(AllocateEvent allocateEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(SubstitutedVMEvent substitutedVMEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(BootNode a) {
        return new BootNode(nodeOffset.get(a.getNode()),
                a.getStart(),
                a.getEnd());
    }

    @Override
    public Object visit(BootVM a) {
        return new BootVM(vmOffset.get(a.getVM()),
                nodeOffset.get(a.getDestinationNode()),
                a.getStart(),
                a.getEnd());
    }

    @Override
    public Object visit(ForgeVM a) {
        return new ForgeVM(vmOffset.get(a.getVM()),
                a.getStart(),
                a.getEnd());
    }

    @Override
    public Object visit(KillVM a) {
        return new KillVM(vmOffset.get(a.getVM()),
                nodeOffset.get(a.getNode()),
                a.getStart(),
                a.getEnd());
    }

    @Override
    public Object visit(MigrateVM a) {
        return new MigrateVM(vmOffset.get(a.getVM()),
                nodeOffset.get(a.getSourceNode()),
                nodeOffset.get(a.getDestinationNode()),
                a.getStart(),
                a.getEnd());
    }

    @Override
    public Object visit(ResumeVM a) {
        return new ResumeVM(vmOffset.get(a.getVM()),
                nodeOffset.get(a.getSourceNode()),
                nodeOffset.get(a.getDestinationNode()),
                a.getStart(),
                a.getEnd());
    }

    @Override
    public Object visit(ShutdownNode a) {
        return new ShutdownNode(nodeOffset.get(a.getNode()),
                a.getStart(),
                a.getEnd());
    }

    @Override
    public Object visit(ShutdownVM a) {
        return new ShutdownVM(vmOffset.get(a.getVM()),
                nodeOffset.get(a.getNode()),
                a.getStart(),
                a.getEnd());
    }

    @Override
    public Object visit(SuspendVM a) {
        return new SuspendVM(vmOffset.get(a.getVM()),
                nodeOffset.get(a.getSourceNode()),
                nodeOffset.get(a.getDestinationNode()),
                a.getStart(),
                a.getEnd());
    }
}
