/*
 * Copyright (c) 2010 Ecole des Mines de Nantes.
 *
 *      This file is part of Entropy.
 *
 *      Entropy is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      Entropy is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.plan.actions;


import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.execution.TimedExecutionGraph;
import entropy.plan.parser.TimedReconfigurationPlanSerializer;
import entropy.plan.visualization.PlanVisualizer;

import java.io.IOException;

/**
 * An action to shutdown an online node.
 *
 * @author Fabien Hermenier
 */
public class Shutdown extends NodeAction {

    /**
     * Create a new time-unbounded shutdown action on an online node.
     *
     * @param n the node to halt
     */
    public Shutdown(Node n) {
        super(n, 0, 0);
    }

    /**
     * Create a new time-bounded shutdown action on an online node.
     *
     * @param n The node to stop
     * @param s the moment the action starts
     * @param f the moment the action is finished
     */
    public Shutdown(Node n, int s, int f) {
        super(n, s, f);
    }

    /**
     * Test the equality with another object.
     *
     * @param obj The object to compare with
     * @return true if o is an instance of Shutdown and if both actions act on the same node
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (obj.getClass() == this.getClass()) {
            return this.getNode().equals(((Shutdown) obj).getNode());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getNode().hashCode();
    }

    /**
     * Textual representation of the action.
     *
     * @return a String
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("shutdown(");
        buffer.append(this.getNode().getName());
        buffer.append(")");
        return buffer.toString();
    }

    /**
     * Put the node offline on a specified configuration.
     *
     * @param c the configuration
     * @return {@code true} if the node was online and is set offline. {@code false} otherwise
     */
    @Override
    public boolean apply(Configuration c) {
        if (c.isOffline(this.getNode())) {
            return false;
        }
        return c.addOffline(this.getNode());
    }


    /**
     * Check the compatibility of the action with a source configuration.
     * The hosting node must be online.
     *
     * @param src the configuration to check
     * @return {@code true} if the action is compatible
     */
    @Override
    public boolean isCompatibleWith(Configuration src) {
        return src.isOnline(getNode());
    }


    /**
     * Check the compatibility of the action with a source and a destination configuration.
     * The node must be online in the source configuration and offline is the destination configuration.
     *
     * @param src the source configuration
     * @param dst the configuration to reach
     * @return true if the action is compatible with the configurations
     */
    @Override
    public boolean isCompatibleWith(Configuration src, Configuration dst) {
        return (!src.isOnline(getNode()) || !dst.isOffline(getNode()));
    }

    /**
     * Insert the action as an incoming action.
     *
     * @param g the graph to use
     * @return true if the insertion succeed
     */
    @Override
    public boolean insertIntoGraph(TimedExecutionGraph g) {
        return g.getLockables(getNode()).add(this);
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