/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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
import btrplace.plan.Action;

import java.util.UUID;

/**
 * An action to destroy a VM that can be in any state.
 *
 * @author Fabien Hermenier
 */
public class KillVM extends Action {

    private UUID id;

    private UUID host;

    /**
     * Make a new action.
     *
     * @param vm   the VM to kill
     * @param host its location if any, {@code null} otherwise
     * @param st   the moment the action starts
     * @param ed   the moment the action ends
     */
    public KillVM(UUID vm, UUID host, int st, int ed) {
        super(st, ed);
        id = vm;
        this.host = host;
    }

    /**
     * Get the VM location.
     *
     * @return the node identifier if the VM is hosted somewhere. Otherwise, {@code null}
     */
    public UUID getNode() {
        return host;
    }

    /**
     * Get the VM to kill.
     *
     * @return the VM identifier
     */
    public UUID getVM() {
        return id;
    }

    @Override
    public boolean applyAction(Model i) {
        return i.getMapping().removeVM(id);
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
        int res = getEnd();
        res = getStart() + 31 * res;
        res = res * 31 + (host != null ? host.hashCode() : 0);
        return id.hashCode() + 31 * res;
    }

    @Override
    public String pretty() {
        return new StringBuilder("killVM(vm=").append(id)
                .append(", node=").append(host)
                .append(')').toString();
    }
}
