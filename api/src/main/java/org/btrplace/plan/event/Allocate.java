/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.Objects;

/**
 * An action to indicate the amount of resources of a given type
 * to allocate to a VM.
 *
 * @author Fabien Hermenier
 */
public class Allocate extends Action implements VMEvent {

  private final Node node;

  private final AllocateEvent ev;

    /**
     * Make a new constraint.
     *
     * @param vm     the VM
     * @param on     the node hosting the VM
     * @param rc     the resource identifier
     * @param amount the minimum amount of resource to allocate
     * @param start  the moment the action starts
     * @param end    the moment the action ends
     */
    public Allocate(VM vm, Node on, String rc, int amount, int start, int end) {
        super(start, end);
        ev = new AllocateEvent(vm, rc, amount);
        this.node = on;
    }

    /**
     * Get the node that is currently hosting the VM.
     *
     * @return the node
     */
    public Node getHost() {
        return node;
    }

    @Override
    public VM getVM() {
        return ev.getVM();
    }

    /**
     * Get the resource identifier.
     *
     * @return a non-empty string
     */
    public String getResourceId() {
        return ev.getResourceId();
    }

    /**
     * Get the amount of resources to allocate to the VM.
     *
     * @return a positive number
     */
    public int getAmount() {
        return ev.getAmount();
    }

    @Override
    public boolean applyAction(Model i) {
        return ev.apply(i);
    }

    @Override
    public String pretty() {
        return "allocate(" + "vm=" + ev.getVM() +
                ", on=" + node +
                ", rc=" + ev.getResourceId() +
                ", amount=" + ev.getAmount()
                + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        Allocate that = (Allocate) o;
        return this.getVM().equals(that.getVM())
                && this.node.equals(that.node)
                && this.getResourceId().equals(that.getResourceId())
                && this.getAmount() == that.getAmount();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), ev.getAmount(), ev.getResourceId(), ev.getVM(), node);
    }

    @Override
    public Object visit(ActionVisitor v) {
        return v.visit(this);
    }
}
