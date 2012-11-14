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
 * An action to start an offline node. Once the execution is finished, the node is online.
 *
 * @author Fabien Hermenier
 */
public class Startup extends Action {

    private UUID node;

    /**
     * Create a new startup action on an offline node.
     *
     * @param n The node to start
     * @param s the moment the action starts
     * @param f the moment the action is finished
     */
    public Startup(UUID n, int s, int f) {
        super(s, f);
    }

    /**
     * Test the equality with another object.
     *
     * @param obj The object to compare with
     * @return true if o is an instance of Startup and if both actions act on the same node
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (obj.getClass() == this.getClass()) {
            Startup that = (Startup) obj;
            return this.node.equals(that.node) &&
                    this.getStart() == that.getStart() &&
                    this.getEnd() == that.getEnd();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int res = getEnd();
        res = getStart() + 31 * res;
        return 31 * res + node.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("startup(");
        buffer.append("node=").append(node).append(")");
        return buffer.toString();
    }

    /**
     * Put the node online on the model.
     *
     * @param c the model to alter
     */
    @Override
    public boolean apply(Model c) {
        c.getMapping().addOnlineNode(node);
        return true;
    }
}
