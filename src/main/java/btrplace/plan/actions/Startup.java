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


import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.execution.TimedExecutionGraph;
import entropy.plan.parser.TimedReconfigurationPlanSerializer;
import entropy.plan.visualization.PlanVisualizer;

import java.io.IOException;

/**
 * An action to start an offline node. Once the execution is finished, the node is online.
 *
 * @author Fabien Hermenier
 */
public class Startup extends NodeAction {

    /**
     * Create a new time-unbounded startup action on an offline node.
     *
     * @param n The node to start
     */
    public Startup(Node n) {
        this(n, 0, 0);
    }

    /**
     * Create a new time-bounded startup action on an offline node.
     *
     * @param n The node to start
     * @param s the moment the action starts
     * @param f the moment the action is finished
     */
    public Startup(Node n, int s, int f) {
        super(n, s, f);
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
            return this.getNode().equals(((Startup) obj).getNode());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getNode().hashCode();
    }

    /**
     * Textual representation of the startup action.
     *
     * @return a String
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("startup(");
        buffer.append(this.getNode().getName());
        buffer.append(")");
        return buffer.toString();
    }

    /**
     * Put the node online on a specific configuration.
     *
     * @param c the configuration
     */
    @Override
    public boolean apply(Configuration c) {
        c.addOnline(this.getNode());
        return true;
    }

    /**
     * Check the compatibility of the action with a source configuration.
     * The hosting node must be offline
     *
     * @param src the configuration to check
     * @return {@code true} if the action is compatible
     */
    @Override
    public boolean isCompatibleWith(Configuration src) {
        return (src.isOffline(getNode()));
    }


    /**
     * Check the compatibility of the action with a source and a destination configuration.
     * The node must be offline in the source configuration and online is the destination configuration.
     *
     * @param src the source configuration
     * @param dst the configuration to reach
     * @return true if the action is compatible with the configurations
     */
    @Override
    public boolean isCompatibleWith(Configuration src, Configuration dst) {
        return (!src.isOffline(getNode()) || !dst.isOnline(getNode()));
    }

    /**
     * Insert the action as an outgoing action. In creates resources!
     *
     * @param g the graph to use
     * @return true if the insertion succeed
     */
    @Override
    public boolean insertIntoGraph(TimedExecutionGraph g) {
        return g.getUnlockings(this.getNode()).add(this);
    }

    @Override
    public void injectToVisualizer(PlanVisualizer vis) {
        vis.inject(this);
    }

    @Override
    public void serialize(TimedReconfigurationPlanSerializer s) throws IOException {
        s.serialize(this);
    }

}
