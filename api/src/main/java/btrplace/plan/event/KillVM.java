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

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;

import java.util.Objects;

/**
 * An action to destroy a VM that can be in any state.
 *
 * @author Fabien Hermenier
 */
public class KillVM extends Action implements VMStateTransition {

    private VM id;

    private Node host;

    /**
     * Make a new action.
     *
     * @param vm    the VM to kill
     * @param on    its location if any, {@code null} otherwise
     * @param start the moment the action starts
     * @param end   the moment the action ends
     */
    public KillVM(VM vm, Node on, int start, int end) {
        super(start, end);
        id = vm;
        this.host = on;
    }

    /**
     * Get the VM location.
     *
     * @return the node if the VM is hosted somewhere.{@code null} otherwise
     */
    public Node getNode() {
        return host;
    }

    @Override
    public VM getVM() {
        return id;
    }

    @Override
    public boolean applyAction(Model i) {
        return i.getMapping().remove(id);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            KillVM that = (KillVM) o;
            return this.id.equals(that.id) &&
                    ((host == null && that.host == null) || (host != null && host.equals(that.host))) &&
                    this.getStart() == that.getStart() &&
                    this.getEnd() == that.getEnd();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), host, id);
    }

    @Override
    public String pretty() {
        return new StringBuilder("killVM(vm=").append(id)
                .append(", node=").append(host)
                .append(')').toString();
    }


    @Override
    public VMState getCurrentState() {
        return VMState.running;
    }

    @Override
    public VMState getNextState() {
        return VMState.killed;
    }

    @Override
    public Object visit(ActionVisitor v) {
        return v.visit(this);
    }
}
