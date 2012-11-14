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
import entropy.configuration.VirtualMachine;
import entropy.execution.TimedExecutionGraph;
import entropy.plan.parser.TimedReconfigurationPlanSerializer;
import entropy.plan.visualization.PlanVisualizer;

import java.io.IOException;

/**
 * Unpause a virtual machine.
 * <p/>
 * TODO: implement
 *
 * @author Fabien Hermenier
 */
public class UnPause extends VirtualMachineAction {

    /**
     * Make a new time-unbounded action.
     *
     * @param v the virtual machine to pause
     * @param n the hosting node
     */
    public UnPause(VirtualMachine v, Node n) {
        this(v, n, -1, -1);
    }

    /**
     * Make a new time bounded action.
     *
     * @param v the virtual machine to pause
     * @param n the hosting node
     * @param s the moment to start the action
     * @param f the finish moment of the action
     */
    public UnPause(VirtualMachine v, Node n, int s, int f) {
        super(v, n, s, f);
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
            UnPause m = (UnPause) o;
            return this.getVirtualMachine().equals(m.getVirtualMachine())
                    && this.getHost().equals(m.getHost());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getVirtualMachine().hashCode() * 31
                + this.getHost().hashCode() * 31;
    }

    /**
     * Check the compatibility of the action with a source configuration.
     * Not implemented
     *
     * @param src the configuration to check
     * @return {@code true} if the action is compatible
     */
    @Override
    public boolean isCompatibleWith(Configuration src) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean apply(Configuration c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Check the compatibility of the action with a source and a destination configuration.
     * Not implemented
     *
     * @param src the source configuration
     * @param dst the configuration to reach
     * @return true if the action is compatible with the configurations
     */
    @Override
    public boolean isCompatibleWith(Configuration src, Configuration dst) {
        throw new UnsupportedOperationException();
    }

    /**
     * Insert the action as an incoming action.
     *
     * @param graph the graph to insert the action into
     * @return true if the insertion succeed
     */
    @Override
    public boolean insertIntoGraph(TimedExecutionGraph graph) {
        return graph.getLockables(getHost()).add(this);
    }

    @Override
    public String toString() {
        return new StringBuilder("unpause(").append(getVirtualMachine().getName()).append(")").toString();
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
