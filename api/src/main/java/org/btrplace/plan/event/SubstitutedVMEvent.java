/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

  private final VM oldVm;
  private final VM newVm;

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
