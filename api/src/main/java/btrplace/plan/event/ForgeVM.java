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

package btrplace.plan.event;

import btrplace.model.Mapping;
import btrplace.model.Model;

import java.util.Objects;

/**
 * Prepare a VM for being deployed.
 *
 * @author Fabien Hermenier
 */
public class ForgeVM extends Action implements VMStateTransition {

    private int id;

    /**
     * Make a new action.
     *
     * @param vm the VM to force.
     */
    public ForgeVM(int vm, int st, int ed) {
        super(st, ed);
        this.id = vm;
    }

    /**
     * Put the VM in the ready state if it does not already belong
     * to the mapping.
     *
     * @param m the model to modify
     * @return {@code true} iff successful
     */
    @Override
    public boolean applyAction(Model m) {
        Mapping map = m.getMapping();

        if (!map.getAllVMs().contains(id)) {
            map.addReadyVM(id);
            return true;
        }
        return false;
    }

    /**
     * Test if this action is equals to another object.
     *
     * @param o the object to compare with
     * @return true if ref is an instance of Force and if both
     *         instance involve the same virtual machine
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            ForgeVM that = (ForgeVM) o;
            return this.id == that.id
                    && this.getStart() == that.getStart()
                    && this.getEnd() == that.getEnd();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), id);
    }

    @Override
    public int getVM() {
        return id;
    }

    @Override
    public String pretty() {
        return new StringBuilder("forge(vm=").append(id).append(')').toString();
    }

    @Override
    public VMState getCurrentState() {
        return VMState.init;
    }

    @Override
    public VMState getNextState() {
        return VMState.ready;
    }

    @Override
    public Object visit(ActionVisitor v) {
        return v.visit(this);
    }
}
