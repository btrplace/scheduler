package org.btrplace.model.view.net;

import org.btrplace.model.Element;

/**
 * Model a switch
 * A switch should not be instantiated directly. Use {@link NetworkView#newSwitch()} instead.
 *
 * @author Vincent Kherbache
 * @see NetworkView#newSwitch()
 */
public class Switch implements Element,PhysicalElement,NetworkElement {

    private int id;
    private int capacity;

    /**
     * Make a new Switch.
     *
     * @param id    the switch identifier.
     * @param c     the maximal capacity of the Switch (c<=0 for a non-blocking switch).
     */
    public Switch(int id, int c) {
        this.id = id;
        capacity = c;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public String toString() {
        return "switch#" + id;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Switch)) {
            return false;
        }

        Switch sw = (Switch) o;

        return id == sw.id();
    }
}
