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

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.event.*;

import java.util.*;

/**
 * A default constraint checker that allow every action and event.
 * In addition, {@link SubstitutedVMEvent} events are tracked and
 * considered to maintain the set of VMs that is involved in
 * the constraint.
 *
 * @author Fabien Hermenier
 */
public abstract class AllowAllConstraintChecker<C extends SatConstraint> implements SatConstraintChecker<C> {

    /**
     * VMs involved in the constraint.
     * Updated after each {@link SubstitutedVMEvent} event.
     */
    private Set<VM> vms;

    /**
     * Nodes involved in the constraint.
     */
    private Set<Node> nodes;

    private C cstr;

    private List<Collection<VM>> tracked;

    /**
     * Make a new checker.
     *
     * @param c the constraint associated to the checker.
     */
    public AllowAllConstraintChecker(C c) {
        this.vms = new HashSet<>(c.getInvolvedVMs());
        this.nodes = new HashSet<>(c.getInvolvedNodes());
        this.cstr = c;
        tracked = new ArrayList<>();
    }

    /**
     * Register a new set of VMs int to track.
     * Each {@link SubstitutedVMEvent} event is caught
     * and all of the registered collections are updated
     * accordingly
     *
     * @param c the collection to register
     * @return {@code true} iff the collection has been added
     */
    public boolean track(Collection<VM> c) {
        return tracked.add(c);
    }

    @Override
    public boolean startsWith(Model mo) {
        return true;
    }

    /**
     * @param a the executed that will be executed
     * @return {@code startRunningVMPlacement(a)}
     */
    @Override
    public boolean start(MigrateVM a) {
        return startRunningVMPlacement(a);
    }

    /**
     * {@inheritDoc}
     * Executes {@code endRunningVMPlacement(a)}
     *
     * @param a the executed that will be executed
     */
    @Override
    public void end(MigrateVM a) {
        endRunningVMPlacement(a);
    }

    /**
     * @param a the executed that will be executed
     * @return {@code startRunningVMPlacement(a)}
     */
    @Override
    public boolean start(BootVM a) {
        return startRunningVMPlacement(a);
    }

    /**
     * {@inheritDoc}
     * Executes {@code endRunningVMPlacement(a)}
     *
     * @param a the executed that will be executed
     */
    @Override
    public void end(BootVM a) {
        endRunningVMPlacement(a);
    }

    @Override
    public boolean start(BootNode a) {
        return true;
    }

    @Override
    public void end(BootNode a) {

    }

    @Override
    public boolean start(ShutdownVM a) {
        return true;
    }

    @Override
    public void end(ShutdownVM a) {

    }

    @Override
    public boolean start(ShutdownNode a) {
        return true;
    }

    @Override
    public void end(ShutdownNode a) {

    }

    @Override
    public boolean start(ResumeVM a) {
        return startRunningVMPlacement(a);
    }

    @Override
    public void end(ResumeVM a) {
        endRunningVMPlacement(a);
    }

    @Override
    public boolean start(SuspendVM a) {
        return true;
    }

    @Override
    public void end(SuspendVM a) {

    }

    @Override
    public boolean start(KillVM a) {
        return true;
    }

    @Override
    public void end(KillVM a) {

    }

    @Override
    public boolean start(ForgeVM a) {
        return true;
    }

    @Override
    public void end(ForgeVM a) {

    }

    @Override
    public boolean endsWith(Model mo) {
        return true;
    }

    @Override
    public boolean consume(SubstitutedVMEvent e) {
        for (Collection<VM> c : tracked) {
            if (c.remove(e.getVM())) {
                c.add(e.getNewVM());
            }
        }
        if (vms.remove(e.getVM())) {
            vms.add(e.getNewVM());
        }
        return true;
    }

    @Override
    public boolean consume(AllocateEvent e) {
        return true;
    }

    @Override
    public boolean start(Allocate e) {
        return true;
    }

    @Override
    public void end(Allocate e) {

    }

    /**
     * Allow all the {@link RunningVMPlacement} actions.
     *
     * @param a the action to allow
     * @return {@code true}
     */
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        return true;
    }

    /**
     * Notify the end of a {@link RunningVMPlacement} action.
     *
     * @param a the notified action
     */
    public void endRunningVMPlacement(RunningVMPlacement a) {

    }

    @Override
    public C getConstraint() {
        return cstr;
    }

    /**
     * Get the VMs involved in the constraint.
     * The set is automatically updated by the {@link SubstitutedVMEvent} events.
     *
     * @return a set of VMs that may be empty
     */
    public Set<VM> getVMs() {
        return vms;
    }

    /**
     * Get the nodes involved in the constraint.
     *
     * @return a set of nodes that may be empty
     */
    public Set<Node> getNodes() {
        return nodes;
    }
}
