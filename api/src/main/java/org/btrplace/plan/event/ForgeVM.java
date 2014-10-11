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

package org.btrplace.plan.event;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.VMState;

import java.util.Objects;

/**
 * Prepare a VM for being deployed.
 *
 * @author Fabien Hermenier
 */
public class ForgeVM extends Action implements VMStateTransition {

    private VM id;

    /**
     * Make a new action.
     *
     * @param vm    the VM to forge.
     * @param start the moment the action starts
     * @param end   the moment the action ends
     */
    public ForgeVM(VM vm, int start, int end) {
        super(start, end);
        this.id = vm;
    }

    /**
     * Put the VM in the ready state iff
     * it does not already belong to the mapping.
     *
     * @param m the model to modify
     * @return {@code true} iff successful
     */
    @Override
    public boolean applyAction(Model m) {
        Mapping map = m.getMapping();

        if (!map.contains(id)) {
            map.addReadyVM(id);
            return true;
        }
        return false;
    }

    /**
     * Test if the action is equals to another object.
     *
     * @param o the object to compare with
     * @return true if {@code o} is an instance of ForgeVM and if both
     * instances involve the same VM
     */
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        ForgeVM that = (ForgeVM) o;
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), id);
    }

    @Override
    public VM getVM() {
        return id;
    }

    @Override
    public String pretty() {
        return "forge(vm=" + id + ')';
    }

    @Override
    public VMState getCurrentState() {
        return VMState.INIT;
    }

    @Override
    public VMState getNextState() {
        return VMState.READY;
    }

    @Override
    public Object visit(ActionVisitor v) {
        return v.visit(this);
    }
}
