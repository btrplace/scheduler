/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.plan.event;

import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.view.ModelView;

import java.util.Objects;

/**
 * A event to inform a cloneable VM
 * has been cloned and is now available using a different identifier.
 *
 * @author Fabien Hermenier
 */
public class SubstitutedVMEvent implements VMEvent {

    private VM oldVm, newVm;

    /**
     * Instantiate a new event.
     *
     * @param vm    the old VM identifier
     * @param newVM the new VM identifier
     */
    public SubstitutedVMEvent(VM vm, VM newVM) {
        oldVm = vm;
        this.newVm = newVM;
    }

    /**
     * Get the old VM.
     *
     * @return a VM
     */
    @Override
    public VM getVM() {
        return oldVm;
    }

    /**
     * Get the new VM.
     *
     * @return a VM.
     */
    public VM getNewVM() {
        return newVm;
    }

    @Override
    public boolean apply(Model m) {
        for (ModelView v : m.getViews()) {
            v.substituteVM(oldVm, newVm);
        }
        return true;
    }

    @Override
    public Object visit(ActionVisitor v) {
        return v.visit(this);
    }

    @Override
    public String toString() {
        return "substitutedVM(" + "vm=" + oldVm + ", newVm=" + newVm + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SubstitutedVMEvent that = (SubstitutedVMEvent) o;
        return newVm.equals(that.newVm) && oldVm.equals(that.oldVm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldVm, newVm);
    }
}
