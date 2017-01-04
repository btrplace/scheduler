/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

import org.btrplace.model.Instance;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Preserve;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.view.CShareableResource;

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
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        CShareableResource map = (CShareableResource) rp.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
        if (map == null) {
            throw new SchedulerException(rp.getSourceModel(), "Unable to get the resource mapper associated to '" +
                    cstr.getResource() + "'");
        }
        VM vm = cstr.getInvolvedVMs().iterator().next();
        if (rp.getFutureRunningVMs().contains(vm)) {
            int idx = rp.getVM(vm);
            map.minVMAllocation(idx, cstr.getAmount());
        }
        return true;
    }

    /**
     * {@inheritDoc}.
     * This implementation is just a stub. A proper estimation will be made directly by {@link CShareableResource#getMisPlacedVMs(Instance)}.
     *
     * @param i the instance to inspect
     * @return an empty set
     */
    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return cstr.toString();
    }
}
