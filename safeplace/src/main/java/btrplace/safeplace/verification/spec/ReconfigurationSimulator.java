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

package btrplace.safeplace.verification.spec;

import btrplace.model.Node;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.event.*;
import btrplace.safeplace.spec.type.NodeStateType;
import btrplace.safeplace.spec.type.VMStateType;

/**
 * @author Fabien Hermenier
 */
public class ReconfigurationSimulator {

    private SpecModel mo;

    public ReconfigurationSimulator(SpecModel mo) {
        this.mo = mo;
    }

    public SpecModel currentModel() {
        return mo;
    }

    public void startsWith(SpecModel mo) {
        this.mo = mo;
    }


    public void start(MigrateVM a) {
        mo.getMapping().state(a.getVM(), VMStateType.Type.migrating);
        mo.getMapping().host(a.getVM(), a.getDestinationNode());
        //Hosted also on distinct node (but running on the old one)
    }


    public void end(MigrateVM a) {
        mo.getMapping().state(a.getVM(), VMStateType.Type.running);
        mo.getMapping().activateOn(a.getVM(), a.getDestinationNode());
        //No longer hosted on the source node
        //running on the new one.
        mo.getMapping().unhost(a.getSourceNode(), a.getVM());
    }


    public void start(BootVM a) {
        mo.getMapping().state(a.getVM(), VMStateType.Type.booting);
        mo.getMapping().host(a.getVM(), a.getDestinationNode());
    }


    public void end(BootVM a) {
        mo.getMapping().state(a.getVM(), VMStateType.Type.running);
    }


    public void start(BootNode a) {
        mo.getMapping().state(a.getNode(), NodeStateType.Type.booting);
    }


    public void end(BootNode a) {
        mo.getMapping().state(a.getNode(), NodeStateType.Type.online);
    }


    public void start(ShutdownVM a) {
        mo.getMapping().state(a.getVM(), VMStateType.Type.halting);
    }


    public void end(ShutdownVM a) {
        mo.getMapping().state(a.getVM(), VMStateType.Type.ready);
        mo.getMapping().unhost(a.getNode(), a.getVM());
        mo.getMapping().desactivate(a.getVM());
    }


    public void start(ShutdownNode a) {
        mo.getMapping().state(a.getNode(), NodeStateType.Type.halting);
    }


    public void end(ShutdownNode a) {
        mo.getMapping().state(a.getNode(), NodeStateType.Type.offline);
    }


    public void start(ResumeVM a) {
        mo.getMapping().state(a.getVM(), VMStateType.Type.resuming);
        mo.getMapping().host(a.getVM(), a.getDestinationNode());
    }


    public void end(ResumeVM a) {
        mo.getMapping().state(a.getVM(), VMStateType.Type.running);
    }


    public void start(SuspendVM a) {
        mo.getMapping().state(a.getVM(), VMStateType.Type.suspending);
    }


    public void end(SuspendVM a) {
        mo.getMapping().state(a.getVM(), VMStateType.Type.sleeping);
    }


    public boolean start(KillVM a) {
        //TODO: terminating ?
        mo.getMapping().state(a.getVM(), VMStateType.Type.terminated);
        return true;
    }


    public void end(KillVM a) {
        Node n = a.getNode();
        if (n != null) {
            mo.getMapping().unhost(n, a.getVM());
            mo.getMapping().desactivate(a.getVM());
            mo.getMapping().state(a.getVM(), VMStateType.Type.terminated);
        }
    }


    public boolean start(ForgeVM a) {
        throw new UnsupportedOperationException("Unsupported action " + a);
    }


    public void end(ForgeVM a) {
        throw new UnsupportedOperationException("Unsupported action " + a);
    }


    public boolean consume(SubstitutedVMEvent e) {
        throw new UnsupportedOperationException("Unsupported action " + e);
    }


    public boolean consume(AllocateEvent e) {
        throw new UnsupportedOperationException("Unsupported action " + e);
    }


    public boolean start(Allocate a) {
        throw new UnsupportedOperationException("Unsupported action " + a);
    }


    public void end(Allocate e) {
        throw new UnsupportedOperationException("Unsupported action " + e);
    }


    public boolean endsWith(SpecModel mo) {
        return true;
    }


    public SatConstraint getConstraint() {
        return null;
        //throw new UnsupportedOperationException("No constraint");
    }
}
