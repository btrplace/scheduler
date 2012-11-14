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
package btrplace.plan.actions;


import btrplace.model.Model;
import btrplace.plan.Action;

import java.util.UUID;

/**
 * Pause a running virtual machine.
 * <p/>
 * TODO: Implement
 *
 * @author Fabien Hermenier
 */
public class Pause extends Action {

    private UUID vm;

    private UUID node;

    /**
     * Make a new time-bounded action.
     *
     * @param v the virtual machine to pause
     * @param n the hosting node
     * @param s the moment to start the action
     * @param f the moment the action ends
     */
    public Pause(UUID v, UUID n, int s, int f) {
        super(s, f);
        vm = v;
        node = n;
    }


    @Override
    public boolean apply(Model i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return new StringBuilder("pause(").append(vm).append(")").toString();
    }

    /**
     * Test if this action is equals to another object.
     *
     * @param o the object to compare with
     * @return true if ref is an instanceof UnPause and if both
     *         instance involve the same virtual machine and the same nodes
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            Pause that = (Pause) o;
            return this.vm.equals(that.vm) &&
                    this.node.equals(that.node) &&
                    this.getStart() == that.getStart() &&
                    this.getEnd() == that.getEnd();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int res = getEnd();
        res = getStart() + 31 * res;
        res = vm.hashCode() + 31 * res;
        return 31 * res + node.hashCode();
    }
}

