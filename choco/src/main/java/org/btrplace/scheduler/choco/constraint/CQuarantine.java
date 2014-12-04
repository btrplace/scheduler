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

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.Quarantine;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.Collections;
import java.util.Set;

/**
 * Choco implementation of {@link org.btrplace.model.constraint.Quarantine}.
 *
 * @author Fabien Hermenier
 */
public class CQuarantine implements ChocoConstraint {

    private Quarantine cstr;

    /**
     * Make a new constraint.
     *
     * @param q the quarantine constraint to rely on
     */
    public CQuarantine(Quarantine q) {
        this.cstr = q;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SchedulerException {
        // It is just a composition of a root constraint on the VMs on the given nodes (the zone)
        // plus a ban on the other VMs to prevent them for being hosted in the zone
        Mapping map = rp.getSourceModel().getMapping();
        Node n = cstr.getInvolvedNodes().iterator().next();
        int nIdx = rp.getNode(n);
        for (VM vm : rp.getFutureRunningVMs()) {
            if (n.equals(map.getVMLocation(vm))) {
                IntVar d = rp.getVMAction(vm).getDSlice().getHoster();
                try {
                    d.instantiateTo(nIdx, Cause.Null);
                } catch (ContradictionException e) {
                    rp.getLogger().error("Unable to root VM '{}' on '{}': {}", vm, n, e.getMessage());
                    return false;
                }
            } else {
                IntVar d = rp.getVMAction(vm).getDSlice().getHoster();
                try {
                    d.removeValue(nIdx, Cause.Null);
                } catch (ContradictionException e) {
                    rp.getLogger().error("Unable to disallow VM '{}' to be hosted on '{}': {}", vm, n, e.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return cstr.toString();
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return Quarantine.class;
        }

        @Override
        public CQuarantine build(Constraint c) {
            return new CQuarantine((Quarantine) c);
        }
    }
}
