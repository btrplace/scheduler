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

package btrplace.solver.choco.constraint;


import btrplace.model.Model;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.view.CShareableResource;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.real.RealInterval;
import choco.kernel.solver.variables.real.RealIntervalConstant;
import choco.kernel.solver.variables.real.RealVar;

import java.util.HashSet;
import java.util.Set;


/**
 * Choco implementation of {@link btrplace.model.constraint.SatConstraint}.
 *
 * @author Fabien Hermenier
 */
public class COverbook implements ChocoSatConstraint {

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
    public boolean inject(ReconfigurationProblem rp) throws SolverException {


        CShareableResource rcm = (CShareableResource) rp.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
        if (rcm == null) {
            throw new SolverException(rp.getSourceModel(), "Unable to get the resource mapping '" + cstr.getResource() + "'");
        }

        for (int u : cstr.getInvolvedNodes()) {
            RealVar v = rcm.getOverbookRatio(rp.getNodeIdx(u));
            RealInterval ric = new RealIntervalConstant(v.getInf(), cstr.getRatio());
            try {
                v.intersect(ric);
            } catch (ContradictionException ex) {
                rp.getLogger().error("Unable to restrict {} to up to {}", v.getName(), cstr.getRatio());
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<Integer> getMisPlacedVMs(Model m) {
        ShareableResource rc = (ShareableResource) m.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
        Set<Integer> bads = new HashSet<>();
        if (rc == null) {
            //No resource given, all the VMs are considered as misplaced
            for (int n : cstr.getInvolvedNodes()) {
                bads.addAll(m.getMapping().getRunningVMs(n));
            }
        } else {
            //Check if the node is saturated
            for (int n : cstr.getInvolvedNodes()) {
                int overCapa = (int) (cstr.getRatio() * rc.getCapacity(n));
                //Minus the VMs usage
                for (int vmId : m.getMapping().getRunningVMs(n)) {
                    overCapa -= rc.getConsumption(vmId);
                    if (overCapa < 0) {
                        bads.addAll(m.getMapping().getRunningVMs());
                        break;
                    }
                }

            }
        }
        return bads;
    }

    @Override
    public String toString() {
        return cstr.toString();
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return Overbook.class;
        }

        @Override
        public COverbook build(SatConstraint cstr) {
            return new COverbook((Overbook) cstr);
        }
    }
}
