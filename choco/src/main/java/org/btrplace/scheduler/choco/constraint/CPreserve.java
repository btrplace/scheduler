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

import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.Preserve;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.view.CShareableResource;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.Collections;
import java.util.Set;


/**
 * Choco implementation of {@link org.btrplace.model.constraint.Preserve}.
 *
 * @author Fabien Hermenier
 */
public class CPreserve implements ChocoConstraint {

    private Preserve cstr;

    /**
     * Make a new constraint.
     *
     * @param p the constraint to rely on
     */
    public CPreserve(Preserve p) {
        cstr = p;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SchedulerException {
        CShareableResource map = (CShareableResource) rp.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
        if (map == null) {
            throw new SchedulerException(rp.getSourceModel(), "Unable to get the resource mapper associated to '" +
                    cstr.getResource() + "'");
        }
        VM vm = cstr.getInvolvedVMs().iterator().next();
        if (rp.getFutureRunningVMs().contains(vm)) {
            int idx = rp.getVM(vm);
            IntVar v = map.getVMsAllocation()[idx];
            try {
                v.updateLowerBound(cstr.getAmount(), Cause.Null);
            } catch (ContradictionException ex) {
                rp.getLogger().error("Unable to set the '{}' consumption for VM '{}' to '{}'", cstr.getResource(), cstr.getAmount(), ex.getMessage());
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        ShareableResource rc = (ShareableResource) m.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
        if (rc == null) {
            return Collections.singleton(cstr.getInvolvedVMs().iterator().next());
        } else {
            VM vm = cstr.getInvolvedVMs().iterator().next();
            int x = rc.getConsumption(vm);
            if (x < cstr.getAmount()) {
                return Collections.singleton(vm);
            }
        }
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return cstr.toString();
    }

    /**
     * The builder associated to that constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return Preserve.class;
        }

        @Override
        public CPreserve build(Constraint c) {
            return new CPreserve((Preserve) c);
        }
    }
}
