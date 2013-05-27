/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

package btrplace.plan.event;

import btrplace.model.Model;
import btrplace.model.view.ModelView;

/**
 * A event to inform a cloneable VM
 * has been cloned and is now available using a different int.
 *
 * @author Fabien Hermenier
 */
public class SubstitutedVMEvent implements VMEvent {

    private int oldint, newint;

    /**
     * Instantiate a new event.
     *
     * @param vm     the old VM int
     * @param newint the new VM int
     */
    public SubstitutedVMEvent(int vm, int newint) {
        oldint = vm;
        this.newint = newint;
    }

    /**
     * Get the old VM identifier.
     *
     * @return a int
     */
    @Override
    public int getVM() {
        return oldint;
    }

    /**
     * Get the new VM identifier.
     *
     * @return a int.
     */
    public int getNewint() {
        return newint;
    }

    @Override
    public boolean apply(Model m) {
        for (ModelView v : m.getViews()) {
            v.substitute(oldint, newint);
        }
        return true;
    }

    @Override
    public Object visit(ActionVisitor v) {
        return v.visit(this);
    }

    @Override
    public String toString() {
        return new StringBuilder("substitutedVM(")
                .append("vm=").append(oldint)
                .append(", newint=").append(newint)
                .append(')').toString();
    }

}
