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
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.Overbook;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.view.CShareableResource;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.RealVar;

import java.util.Collections;
import java.util.Set;


/**
 * Choco implementation of {@link org.btrplace.model.constraint.SatConstraint}.
 *
 * @author Fabien Hermenier
 */
public class COverbook implements ChocoConstraint {

    private Overbook cstr;

    /**
     * Make a new constraint.
     *
     * @param o the constraint to rely on
     */
    public COverbook(Overbook o) {
        cstr = o;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SchedulerException {


        CShareableResource rcm = (CShareableResource) rp.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
        if (rcm == null) {
            throw new SchedulerException(rp.getSourceModel(), "Unable to get the resource mapping '" + cstr.getResource() + "'");
        }

        Node u = cstr.getInvolvedNodes().iterator().next();
        RealVar v = rcm.getOverbookRatio(rp.getNode(u));

        try {
            v.updateUpperBound(cstr.getRatio(), Cause.Null);
        } catch (ContradictionException ex) {
            rp.getLogger().error("Unable to restrict {} to up to {}", v.getName(), cstr.getRatio());
            return false;
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        ShareableResource rc = (ShareableResource) m.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
        if (rc == null) {
            //No resource given, all the VMs are considered as misplaced
            Node n = cstr.getInvolvedNodes().iterator().next();
            return m.getMapping().getRunningVMs(n);
        } else {
            //Check if the node is saturated
            Node n = cstr.getInvolvedNodes().iterator().next();
            int maxVCapacity = (int) (cstr.getRatio() * rc.getCapacity(n));
            //Minus the VMs usage
            for (VM vmId : m.getMapping().getRunningVMs(n)) {
                maxVCapacity -= rc.getConsumption(vmId);
                if (maxVCapacity < 0) {
                    return m.getMapping().getRunningVMs(n);
                }
            }

        }
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
            return Overbook.class;
        }

        @Override
        public COverbook build(Constraint c) {
            return new COverbook((Overbook) c);
        }
    }
}
