/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.VMState;

import java.util.Objects;

/**
 * An action to destroy a VM that can be in any state.
 *
 * @author Fabien Hermenier
 */
public class KillVM extends Action implements VMStateTransition {

  private final VM id;

  private final Node host;

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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KillVM killVM = (KillVM) o;
        return Objects.equals(id, killVM.id) && Objects.equals(host, killVM.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), host, id);
    }

    @Override
    public String pretty() {
        return "killVM(vm=" + id + ", node=" + host + ')';
    }


    @Override
    public VMState getCurrentState() {
        return VMState.RUNNING;
    }

    @Override
    public VMState getNextState() {
        return VMState.KILLED;
    }

    @Override
    public Object visit(ActionVisitor v) {
        return v.visit(this);
    }
}
